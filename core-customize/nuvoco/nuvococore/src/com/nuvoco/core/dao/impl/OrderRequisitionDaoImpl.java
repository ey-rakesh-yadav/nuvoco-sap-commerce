package com.nuvoco.core.dao.impl;

import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.dao.DataConstraintDao;
import com.nuvoco.core.dao.OrderRequisitionDao;
import com.nuvoco.core.enums.RequisitionStatus;
import com.nuvoco.core.enums.ServiceType;
import com.nuvoco.core.model.DealerRetailerMapModel;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.OrderRequisitionModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class OrderRequisitionDaoImpl extends DefaultGenericDao<OrderRequisitionModel> implements OrderRequisitionDao {


    public OrderRequisitionDaoImpl() {
        super(OrderRequisitionModel._TYPECODE);
    }
    @Resource
    FlexibleSearchService flexibleSearchService;

    @Autowired
    UserService userService;

    @Autowired
    DataConstraintDao dataConstraintDao;

    @Autowired
    PaginatedFlexibleSearchService paginatedFlexibleSearchService;

    /**
     * @param toCustomerList
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<List<Object>> getSalsdMTDforRetailer(List<NuvocoCustomerModel> toCustomerList, String startDate, String endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {p.toCustomer},sum({p.quantity}) from {OrderRequisition as p} ")
                .append(" where {p.deliveredDate}>=?startDate and {p.deliveredDate}<=?endDate ")
                .append(" and {p.toCustomer} in (?toCustomerList) and {p:status}=?status group by {p.toCustomer} ");
        params.put("toCustomerList", toCustomerList);
        params.put("status", RequisitionStatus.DELIVERED);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(NuvocoCustomerModel.class, Double.class));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        List<List<Object>> result = searchResult.getResult();
        return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }

    /**
     * @param requisitionId
     * @return
     */
    @Override
    public OrderRequisitionModel findByRequisitionId(String requisitionId) {
        if(requisitionId != null && !(requisitionId.isEmpty())) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(OrderRequisitionModel.REQUISITIONID, requisitionId);

            final List<OrderRequisitionModel> orderRequisitionList = this.find(map);
            if(orderRequisitionList!= null && !(orderRequisitionList.isEmpty())) {
                return orderRequisitionList.get(0);
            }
        }
        return null;
    }

    /**
     * @param code
     * @return
     */
    @Override
    public OrderModel findOrderByCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {Order} where {code} = ?code and {versionId} is null");

        params.put("code", code);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderModel.class));
        query.addQueryParameters(params);
        final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    /**
     * @param dealer
     * @param retailer
     * @param brand
     * @return
     */
    @Override
    public DealerRetailerMapModel getDealerforRetailerDetails(NuvocoCustomerModel dealer, NuvocoCustomerModel retailer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {DealerRetailerMap} where {dealer}=?dealer and {retailer}=?retailer and {brand} = ?brand");

        params.put("dealer", dealer);
        params.put("retailer", retailer);
        params.put("brand", brand);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(DealerRetailerMapModel.class));
        query.addQueryParameters(params);
        final SearchResult<DealerRetailerMapModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    /**
     * @param statuses
     * @param submitType
     * @param fromDate
     * @param toDate
     * @param currentUser
     * @param productCode
     * @param searchPageData
     * @param requisitionId
     * @param searchKey
     * @return
     */
    @Override
    public SearchPageData<OrderRequisitionModel> getOrderRequisitionDetails(List<String> statuses, String submitType, String fromDate, String toDate, NuvocoCustomerModel currentUser, String productCode, SearchPageData searchPageData, String requisitionId, String searchKey)
    {

        final StringBuilder sql = new StringBuilder();
        final Map<String, Object> map = new HashMap<String, Object>();

        List<RequisitionStatus> requisitionStatuses = new ArrayList<>();

        if(currentUser!=null) {
            if(currentUser.getGroups().contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                if(searchKey!=null && !searchKey.isEmpty()) {
                    sql.append("select {or:pk} from {OrderRequisition as or join NuvocoCustomer as cust on {or:fromCustomer}={cust:pk}}" +
                            " where {toCustomer} = ?retailer and {or:isRequisitionPlaced} = ?requisitionPlaced and {cust:name} like ?searchKey or {cust:uid} like ?searchKey or {cust:customerNo} like ?searchKey or {or:requisitionId} like ?searchKey");
                    map.put("searchKey", searchKey.toUpperCase() + "%");
                    map.put("requisitionPlaced", Boolean.TRUE);
                }
                else {
                    sql.append("select {pk} from {OrderRequisition} where " +
                            " {toCustomer} = ?retailer");
                }
                map.put("retailer", currentUser);
            }
            else if(currentUser.getGroups().contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                if(productCode != null && !productCode.isEmpty()) {
                    sql.append("select {or:pk} from {OrderRequisition as or join Product as p on {or:product}={p:pk}} where " +
                            " {fromCustomer} = ?dealer and {p:code} = ?productCode");
                    map.put("productCode", productCode);
                }
                else if(searchKey!=null && !searchKey.isEmpty()) {
                    sql.append("Select {or:pk} from {OrderRequisition as or join NuvocoCustomer as cust on {or:toCustomer}={cust:pk}}" +
                            " where {fromCustomer} = ?dealer and {or:isRequisitionPlaced} = ?requisitionPlaced and {cust:name} like ?searchKey or {cust:uid} like ?searchKey or {cust:customerNo} like ?searchKey or {or:requisitionId} like ?searchKey");
                    if(submitType.toLowerCase().equals("draft")) {
                        map.put("requisitionPlaced",Boolean.FALSE);
                    }
                    else {
                        map.put("requisitionPlaced",Boolean.TRUE);
                    }
                    map.put("searchKey", searchKey.toUpperCase() + "%");
                }
                else {
                    sql.append("select {pk} from {OrderRequisition} where " +
                            " {fromCustomer} = ?dealer");
                }
                map.put("dealer", currentUser);
                submitType = "all";

                sql.append(" and ({serviceType} = ?serviceType  or {acceptedDate} is null) ");
                map.put("serviceType", ServiceType.CLUBBED_PLACED);

            }
            if(fromDate==null || toDate==null) {
                Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_REQUISTION_LISTING_VISIBLITY");
                sql.append(" and ")
                        .append(getLastXDayQuery("requisitionDate", map, lastXDays));
            }
            else {
                sql.append(" and {requisitionDate} >= ?fromDate and {requisitionDate} < ?toDate ");
                map.put("fromDate",fromDate);
                map.put("toDate",toDate);
            }

        }

        if(!submitType.isEmpty() && submitType!=null) {
            if(submitType.equals("draft") || submitType.equals("Draft")) {
                map.put("requisitionPlaced", Boolean.FALSE);
            } else {
                map.put("requisitionPlaced", Boolean.TRUE);
            }
            sql.append(" and {isRequisitionPlaced} = ?requisitionPlaced");
        }

        if(requisitionId!=null && !requisitionId.isEmpty()) {
            sql.append(" and {requisitionId} = ?requisitionId");
            map.put("requisitionId",requisitionId);
        }

        if(statuses!=null && !statuses.isEmpty()) {
            for(String status : statuses) {
                requisitionStatuses.add(RequisitionStatus.valueOf(status));
            }
            map.put("requisitionStatuses",requisitionStatuses);
            sql.append(" and {status} in (?requisitionStatuses)");
        }

        sql.append(" order by {requisitionDate} desc");

        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Collections.singletonList(OrderRequisitionModel.class));
        query.getQueryParameters().putAll(map);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }


    public static String getLastXDayQuery(String columnName, Map<String, Object> map, Integer lastXDay) {
        LocalDate startDate = LocalDate.now().minusDays(lastXDay);
        LocalDate endDate = LocalDate.now().plusDays(1);
        return getDateQuery(columnName, startDate, endDate, map);
    }

    private static String getDateQuery(String columnName, LocalDate firstDayOfMonth, LocalDate lastDayOfMonth, Map<String, Object> map) {
        StringBuilder builder = new StringBuilder();
        builder.append(" {").append(columnName)
                .append("} >= ?start and {").append(columnName).append("} < ?end ");

        map.put("start", getStartDateConstraint(firstDayOfMonth));
        map.put("end", getStartDateConstraint(lastDayOfMonth));

        return builder.toString();
    }

    public static Date getStartDateConstraint(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = localDate.atStartOfDay(zone);
        Date date = Date.from(dateTime.toInstant());
        return date;
    }

}
