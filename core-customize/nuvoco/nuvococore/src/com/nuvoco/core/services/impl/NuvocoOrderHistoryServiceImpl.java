package com.nuvoco.core.services.impl;

import com.nuvoco.core.enums.OrderType;
import com.nuvoco.core.enums.WarehouseType;
import com.nuvoco.core.services.NuvocoOrderHistoryService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class NuvocoOrderHistoryServiceImpl implements NuvocoOrderHistoryService {


    @Autowired
    UserService userService;

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    PaginatedFlexibleSearchService paginatedFlexibleSearchService;
    /**
     * @param paginationData
     * @param sourceType
     * @param filter
     * @param month
     * @param year
     * @param productName
     * @param orderType
     * @param status
     * @return
     */
    @Override
    public SearchPageData<OrderEntryModel> getTradeOrderListing(SearchPageData paginationData, String sourceType, String filter, int month, int year, String productName, String orderType, String status) {
        final UserModel user = userService.getCurrentUser();
        Date startDate = new Date();
        Date endDate = new Date();
        if(month==0 && year==0) {
            LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
            startDate = getDateConstraint(firstDayOfMonth);
            Calendar cal = Calendar.getInstance();
            cal.setTime(getDateConstraint(lastDayOfMonth));
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            endDate = cal.getTime();

        }
        else {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month-1, 1, 0, 0, 0);
            startDate=cal.getTime();
            cal.set(year, month-1, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
            endDate=cal.getTime();
        }
        OrderType orderTypeEnum = null;
        if(StringUtils.isNotBlank(orderType)){
            orderTypeEnum = OrderType.valueOf(orderType);
        }
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        return getTradeOrderListing(paginationData, sourceType, user, startDate, endDate, site, filter,productName,orderTypeEnum,status);

    }

    protected static Date getDateConstraint(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = localDate.atStartOfDay(zone);
        Date date = Date.from(dateTime.toInstant());
        return date;
    }


    private SearchPageData<OrderEntryModel> getTradeOrderListing(SearchPageData searchPageData, String sourceType, UserModel user, Date startDate, Date endDate, BaseSiteModel site, String filter, String productName, OrderType orderType, String status)
    {
        WarehouseType type = null;

        if(Objects.nonNull(sourceType))
        {
            if(sourceType.equals("Primary"))
            {
                type=WarehouseType.PLANT;
            }
            else if(sourceType.equals("Secondary"))
            {
                type=WarehouseType.DEPOT;
            }
        }

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {oe.pk} FROM { OrderEntry AS oe JOIN Order AS o ON {o.pk}={oe.order} ");
        if(Objects.nonNull(type))
        {
            builder.append("JOIN Warehouse AS w ON {w.pk}={oe.source} ");
        }
        if(Objects.nonNull(filter))
        {
            builder.append(" JOIN NuvocoCustomer AS u ON {u.pk}={o.user} ");
        }
        builder.append("} WHERE   ");
        if(Objects.nonNull(status) && status.equals(OrderStatus.TRUCK_DISPATCHED.getCode()))
        {
            builder.append(" {oe.truckDispatcheddate} BETWEEN ?startDate AND ?endDate ");
        }
        else {
            builder.append(" {oe.deliveredDate} BETWEEN ?startDate AND ?endDate ");
        }

        if(Objects.nonNull(type))
        {
            builder.append("AND {w.type}=?type ");
            params.put("type", type);
        }
        if(Objects.nonNull(filter))
        {
            builder.append("AND (UPPER({o.code}) LIKE ?filter OR UPPER({u.name}) LIKE ?filter OR UPPER({u.uid}) LIKE ?filter OR UPPER({u.customerNo}) LIKE ?filter) ");
            params.put("filter", "%"+filter.toUpperCase()+"%");
        }
        if(null!= orderType){
            builder.append(" AND {o.orderType} = ?orderType ");
            params.put("orderType",orderType);
        }
        if(Objects.nonNull(status) && status.equals(OrderStatus.TRUCK_DISPATCHED.getCode())){
            builder.append(" ORDER BY {oe.truckDispatcheddate} DESC");
        }
        else{
            builder.append(" ORDER BY {oe.deliveredDate} DESC ");
        }

        params.put("startDate", startDate);
        params.put("endDate", endDate);

        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);

        return paginatedFlexibleSearchService.search(parameter);
    }
}
