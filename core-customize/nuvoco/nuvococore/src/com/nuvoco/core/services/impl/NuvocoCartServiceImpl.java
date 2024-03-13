package com.nuvoco.core.services.impl;

import com.nuvoco.core.dao.DeliveryModeDao;
import com.nuvoco.core.enums.CustomerCategory;
import com.nuvoco.core.enums.OrderType;
import com.nuvoco.core.model.DestinationSourceMasterModel;
import com.nuvoco.core.services.NuvocoCartService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.log4j.Logger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

public class NuvocoCartServiceImpl implements NuvocoCartService {


    private static final Logger LOG = Logger.getLogger(NuvocoCartServiceImpl.class);

    @Autowired
    ProductService productService;

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    DeliveryModeDao deliveryModeDao;

    @Autowired
    FlexibleSearchService flexibleSearchService;

    @Autowired
    private PaginatedFlexibleSearchService paginatedFlexibleSearchService;



    /**
     * @param city
     * @param orderType
     * @param deliveryMode
     * @param productCode
     * @param district
     * @param state
     * @param taluka
     * @return
     */
    @Override
    public List<DestinationSourceMasterModel> fetchDestinationSourceByCity(String city, String orderType, String deliveryMode, String productCode, String district, String state, String taluka) {
        List<DestinationSourceMasterModel> list = new ArrayList<>();
        ProductModel product = productService.getProductForCode(productCode);
        String grade = product.getGrade();
        String packaging= product.getBagType();

        if(null == deliveryMode)
        {
            deliveryMode= "ROAD";
        }

        List<DeliveryModeModel> deliveryModeList = deliveryModeDao.findDeliveryModesByCode(deliveryMode);
        list = findDestinationSourceByCode(city, deliveryModeList.get(0), OrderType.valueOf(orderType), CustomerCategory.TR ,grade, packaging, district, state, baseSiteService.getCurrentBaseSite(), taluka);
        LOG.info(String.format("Destination Source Master List Retrieved from DB Call ::%s",list.size()));
        return list;
    }

    /**
     * @param user
     * @param searchPageData
     * @param filter
     * @param month
     * @param year
     * @param productName
     * @param orderType
     * @return
     */
    @Override
    public SearchPageData<CartModel> getSavedCartsBySavedBy(UserModel user, SearchPageData searchPageData, String filter, int month, int year, String productName, String orderType) {
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
        return getSavedCartsBySavedBy(user, searchPageData, filter, startDate, endDate,productName,orderTypeEnum);

    }


    protected static Date getDateConstraint(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = localDate.atStartOfDay(zone);
        Date date = Date.from(dateTime.toInstant());
        return date;
    }

    private SearchPageData<CartModel> getSavedCartsBySavedBy(UserModel user, SearchPageData searchPageData, String filter, Date startDate, Date endDate, String productName, OrderType orderType) {
        validateParameterNotNull(user, "Customer must not be null");
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {c.pk} from {Cart AS c ");
        if(Objects.nonNull(filter))
        {
            builder.append("JOIN B2BCustomer AS u ON {u.pk} = {c.user} LEFT JOIN WAREHOUSE as w on {w:pk} = {c:destination}");
        }
        builder.append("} WHERE {c.savedBy} = ?currentUser AND {c.date} BETWEEN ?startDate AND ?endDate ");
        params.put("currentUser", user);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        if(Objects.nonNull(filter))
        {
            builder.append("AND (UPPER({w:code}) like ?filter OR UPPER({u.name}) LIKE ?filter OR UPPER({u.uid}) LIKE ?filter)");
            params.put("filter", "%"+filter.toUpperCase()+"%");
        }
        if(null != productName) {
            List<String> productList = Arrays.asList(productName.split(","));
            if (productList != null && !productList.isEmpty()) {
                builder.append(" and {c:productName} in (?productList) ");
                params.put("productList", productList);
            }
        }
        if(null!= orderType){
            builder.append(" AND {c.orderType} = ?orderType ");
            params.put("orderType",orderType);
        }
        builder.append("Order By {c.saveTime} DESC");
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }


    private List<DestinationSourceMasterModel> findDestinationSourceByCode(String city, DeliveryModeModel deliveryMode, OrderType orderType, CustomerCategory customerCategory, String grade, String packaging, String district, String state, BaseSiteModel brand, String taluka) {

        if(city!= null && district!= null && state!=null) {
            Map<String, Object> map = new HashMap<>();
            map.put(DestinationSourceMasterModel.DELIVERYMODE, deliveryMode);
            map.put(DestinationSourceMasterModel.BRAND, brand);
            map.put(DestinationSourceMasterModel.ORDERTYPE, orderType);
            map.put(DestinationSourceMasterModel.CUSTOMERCATEGORY, customerCategory);
            map.put(DestinationSourceMasterModel.DESTINATIONCITY, city.toUpperCase());
            if(StringUtils.isNotBlank(taluka)) {
                map.put(DestinationSourceMasterModel.DESTINATIONTALUKA, taluka.toUpperCase());
            }
            map.put(DestinationSourceMasterModel.GRADE, grade);
            map.put(DestinationSourceMasterModel.PACKAGING, packaging);
            map.put(DestinationSourceMasterModel.DESTINATIONDISTRICT, district.toUpperCase());
            map.put(DestinationSourceMasterModel.DESTINATIONSTATE, state.toUpperCase());
            String queryResult = "SELECT {ds:pk} from {DestinationSourceMaster as ds} where {ds:brand}=?brand and {ds:customerCategory}=?customerCategory and {ds:deliveryMode}=?deliveryMode and {ds:orderType}=?orderType and UPPER({ds:destinationState})=?destinationState and UPPER({ds:destinationDistrict})=?destinationDistrict " ;
            if(StringUtils.isNotBlank(taluka)) {
                queryResult = queryResult + " and UPPER({ds:destinationTaluka})=?destinationTaluka ";
            }
            queryResult = queryResult + " and UPPER({ds:destinationCity})=?destinationCity and {ds:grade}=?grade and {ds:packaging}=?packaging ";
            LOG.info(String.format("Query Executed %s",queryResult));
            final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
            query.getQueryParameters().putAll(map);
            final SearchResult<DestinationSourceMasterModel> result = flexibleSearchService.search(query);
             LOG.info(String.format("Query Result %s",result.getResult()));
            return result.getResult();
        }
        return Collections.emptyList();
    }
}
