package com.nuvoco.core.dao.impl;


import com.nuvoco.core.dao.NuvocoOrderCountDao;
import com.nuvoco.core.enums.EpodStatus;
import com.nuvoco.core.enums.OrderType;
import com.nuvoco.core.enums.SPApprovalStatus;
import com.nuvoco.core.model.DataConstraintModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.BaseStoreModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class NuvocoOrderCountDaoImpl extends DefaultGenericDao<DataConstraintModel> implements NuvocoOrderCountDao {

    public  NuvocoOrderCountDaoImpl() {
        super(DataConstraintModel._TYPECODE);
    }

    @Autowired
    PaginatedFlexibleSearchService paginatedFlexibleSearchService;

    @Autowired
    UserService userService;

    /**
     * @param user
     * @param store
     * @param status
     * @param searchPageData
     * @param isCreditLimitBreached
     * @param spApprovalFilter
     * @param approvalPending
     * @return
     */
    @Override
    public SearchPageData<OrderModel> findOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("statusList", Arrays.asList(status));
        //attr.put(OrderModel.USER, user);
        attr.put("isCreditLimitBreached", isCreditLimitBreached);
        Integer lastXDays = findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT {o:pk} from { ").append(OrderModel._TYPECODE).append(" as o} WHERE   ")
                .append(getLastXDayQuery("o:modifiedtime", attr, lastXDays)).append(" and {o:status} in (?statusList) ");
        if(isCreditLimitBreached){
            sql.append(" and {o:creditLimitBreached} = ?isCreditLimitBreached ");
        }
       /* if(approvalPending) {
            sql.append(" and {o:approvalLevel} = ?approvalLevel ");
            getApprovalLevelByUser(user,attr);
        }*/
        sql.append(appendSpApprovalActionQuery(attr, user, spApprovalFilter));

        sql.append(" ORDER BY {o:modifiedtime} DESC ");
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Collections.singletonList(OrderModel.class));
        query.getQueryParameters().putAll(attr);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }



    public String appendSpApprovalActionQuery(Map<String, Object> param, UserModel user, String spApprovalFilter) {
        String queryResult = "";
        if(spApprovalFilter!=null && (spApprovalFilter.equals("approved") || spApprovalFilter.equals("rejected"))) {
            queryResult = queryResult.concat(" and {o.spApprovalStatus}=?spApprovalStatus ");
            queryResult = queryResult.concat(" and {o.spApprovalActionBy}=?spApprovalActionBy ");
            if(spApprovalFilter.equals("approved")) {
                param.put("spApprovalStatus", SPApprovalStatus.APPROVED);
            }
            else {
                param.put("spApprovalStatus", SPApprovalStatus.REJECTED);
            }
            param.put("spApprovalActionBy", (B2BCustomerModel)user);
        }
        return queryResult;
    }
    /**
     * @param user
     * @param store
     * @param status
     * @param searchPageData
     * @param spApprovalFilter
     * @return
     */
    @Override
    public SearchPageData<OrderEntryModel> findOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String spApprovalFilter) {
       return null;

    }

    /**
     * @param user
     * @param store
     * @param status
     * @param searchPageData
     * @param filter
     * @param productName
     * @param orderType
     * @param isCreditLimitBreached
     * @param spApprovalFilter
     * @param approvalPending
     * @return
     */
    @Override
    public SearchPageData<OrderModel> findOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter, String productName, OrderType orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("statusList", Arrays.asList(status));
        attr.put("store", store);
        //attr.put(OrderModel.USER, user);
        attr.put("isCreditLimitBreached", isCreditLimitBreached);
        Integer lastXDays = findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
        String queryResult= "SELECT {o:pk} from {ORDER as o JOIN NuvocoCustomer as u on {u:pk}={o:user} } WHERE " + getLastXDayQuery("o:modifiedtime", attr, lastXDays) + " and {o:status} in (?statusList) " ;

        if(null != productName) {
            List<String> productList = Arrays.asList(productName.split(","));
            if (productList != null && !productList.isEmpty()) {
                queryResult = queryResult.concat(" and {o:productName} in (?productList) ");
                attr.put("productList", productList);
            }
        }
        if(StringUtils.isNotBlank(filter)){
            queryResult = queryResult.concat(" and (UPPER({o:code}) like ?filter OR UPPER({u:uid}) like ?filter OR UPPER({u:name}) like ?filter OR UPPER({u:customerNo}) like ?filter) ");
            attr.put("filter","%"+filter.toUpperCase()+"%");
        }

        if(null!= orderType){
            queryResult = queryResult.concat(" and {o:orderType} = ?orderType ");
            attr.put("orderType",orderType);
        }
        if(isCreditLimitBreached){
            queryResult.concat(" and {o:creditLimitBreached} = ?isCreditLimitBreached ");
        }

        if(approvalPending) {
            queryResult = queryResult.concat(" and {o:approvalLevel} = ?approvalLevel ");
            getApprovalLevelByUser(user,attr);
        }

        queryResult = queryResult.concat(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
        queryResult = queryResult.concat(" ORDER BY {o:modifiedtime} DESC");

        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
        query.setResultClassList(Collections.singletonList(OrderModel.class));
        query.getQueryParameters().putAll(attr);
        parameter.setFlexibleSearchQuery(query);

        return paginatedFlexibleSearchService.search(parameter);
    }

    /**
     * @param user
     * @param store
     * @param status
     * @param searchPageData
     * @param filter
     * @param productName
     * @param orderType
     * @param spApprovalFilter
     * @return
     */
    @Override
    public SearchPageData<OrderEntryModel> findOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter, String productName, OrderType orderType, String spApprovalFilter) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("statusList", Arrays.asList(status));
        attr.put("store", store);
        //attr.put(OrderModel.USER, user);
        Integer lastXDays = findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
        String queryResult=" SELECT {oe:pk} FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN NuvocoCustomer as u on {u:pk}={o:user}} WHERE "
                + getLastXDayQuery("oe:modifiedtime", attr, lastXDays) + " AND {oe:status} IN (?statusList) ";

        if(null != productName) {
            List<String> productList = Arrays.asList(productName.split(","));
            if (productList != null && !productList.isEmpty()) {
                queryResult = queryResult.concat(" and {o:productName} in (?productList) ");
                attr.put("productList", productList);
            }
        }

        if(StringUtils.isNotBlank(filter)){
            queryResult = queryResult.concat(" and (UPPER({o:code}) like ?filter OR UPPER({u:uid}) like ?filter OR UPPER({u:name}) like ?filter OR UPPER({u:customerNo}) like ?filter) ");
            attr.put("filter","%"+filter.toUpperCase()+"%");
        }
        if(null!= orderType){
            queryResult = queryResult.concat(" and {o:orderType} = ?orderType ");
            attr.put("orderType",orderType);
        }
        queryResult = queryResult.concat(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
        queryResult = queryResult.concat(" ORDER BY {oe:modifiedtime} DESC  ");
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
        query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
        query.getQueryParameters().putAll(attr);
        parameter.setFlexibleSearchQuery(query);

        return paginatedFlexibleSearchService.search(parameter);
    }

    /**
     * @param constraintName
     * @return
     */
    @Override
    public Integer findDaysByConstraintName(String constraintName) {
        if(constraintName!=null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(DataConstraintModel.CONSTRAINTNAME, constraintName);

            final List<DataConstraintModel> dataList = this.find(map);
            if(dataList!=null && !dataList.isEmpty())
                return dataList.get(0).getDay();
        }
        return null;
    }

    /**
     * @param user
     * @param attr
     */
    @Override
    public void getApprovalLevelByUser(UserModel user, Map<String, Object> attr) {
       /* if(user.getGroups().contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID))) {
            attr.put("approvalLevel",TerritoryLevels.SUBAREA);
        } else if (user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSM_GROUP_ID))) {
            attr.put("approvalLevel",TerritoryLevels.DISTRICT);
        } else if (user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RH_GROUP_ID))) {
            attr.put("approvalLevel",TerritoryLevels.REGION);
        }*/
    }

    /**
     * @param user
     * @param status
     * @param approvalPending
     * @return
     */
    @Override
    public Integer findOrdersByStatusForSO(UserModel user, OrderStatus[] status, Boolean approvalPending) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("statusList", Arrays.asList(status));
        //attr.put(OrderModel.PLACEDBY, user);
        Integer lastXDays = findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
        //attr.put(OrderModel.SUBAREAMASTER, territoryService.getCurrentTerritory());
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT({o:pk}) from { ").append(OrderModel._TYPECODE).append(" as o} WHERE ")
                .append(getLastXDayQuery("modifiedtime", attr, lastXDays)).append(" and {o:status} in (?statusList)  ");

        if(approvalPending) {
            sql.append(" and {o:approvalLevel} = ?approvalLevel ");
            getApprovalLevelByUser(user,attr);
        }
        //sql.append(" AND {o.subAreaMaster} in (?subAreaMaster) ");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);
        final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
        return result.getResult().get(0);
    }

    /**
     * @param user
     * @param status
     * @return
     */
    @Override
    public Integer findOrderEntriesByStatusForSO(UserModel user, OrderStatus[] status) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("statusList", Arrays.asList(status));
        //attr.put(OrderModel.PLACEDBY, user);
        Integer lastXDays = findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT({oe:pk}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} } WHERE ")
                .append(getLastXDayQuery("oe:modifiedtime", attr, lastXDays)).append(" AND {oe:status} IN (?statusList) ");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);
        final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
        return result.getResult().get(0);
    }

    /**
     * @param currentUser
     * @param status
     * @return
     */
    @Override
    public Integer findCancelOrdersByStatusForSO(UserModel currentUser, OrderStatus[] status) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("statusList", Arrays.asList(status));
        Integer lastXDays = findDaysByConstraintName("CANCELLED_ORDER_VISIBLITY");

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT({o:pk}) from { ").append(OrderModel._TYPECODE).append(" as o} WHERE ")
                .append(getLastXDayQuery("o:cancelledDate", attr, lastXDays)).append(" and {o:status} in (?statusList) ");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);
        final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
        return result.getResult().get(0);
    }

    /**
     * @param currentUser
     * @param status
     * @return
     */
    @Override
    public Integer findCancelOrderEntriesByStatusForSO(UserModel currentUser, OrderStatus[] status) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("statusList", Arrays.asList(status));
        Integer lastXDays = findDaysByConstraintName("CANCELLED_ORDER_VISIBLITY");

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT({oe:pk}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}} WHERE ")
                .append(getLastXDayQuery("oe:cancelledDate", attr, lastXDays)).append(" AND {oe:status} IN (?statusList) ");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);
        final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
        return result.getResult().get(0);
    }



    @Override
    public SearchPageData<OrderModel> findCancelOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter ,String productName , OrderType orderType, String spApprovalFilter, String monthYear) {

        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("statusList", Arrays.asList(status));
        Integer lastXDays = findDaysByConstraintName("CANCELLED_ORDER_VISIBLITY");
        String queryResult= "SELECT {o:pk} from {ORDER as o JOIN NuvocoCustomer as u on {u:pk}={o:user} } WHERE ";

        if(monthYear!=null) {
            queryResult = queryResult.concat("{o:cancelledDate} like ?monthYear ");
            attr.put("monthYear", monthYear);
        }
        else {
            queryResult = queryResult.concat(getLastXDayQuery("o:cancelledDate", attr, lastXDays));
        }
        queryResult = queryResult.concat(" and {o:status} in (?statusList)");

        if(null != productName) {
            List<String> productList = Arrays.asList(productName.split(","));
            if (productList != null && !productList.isEmpty()) {
                queryResult = queryResult.concat(" and {o:productName} in (?productList) ");
                attr.put("productList", productList);
            }
        }

        if(StringUtils.isNotBlank(filter)){
            queryResult = queryResult.concat(" and (UPPER({o:code}) like ?filter OR UPPER({u:uid}) like ?filter OR UPPER({u:name}) like ?filter OR UPPER({u:customerNo}) like ?filter) ");
            attr.put("filter","%"+filter.toUpperCase()+"%");
        }

        if(null!= orderType){
            queryResult = queryResult.concat(" and {o:orderType} = ?orderType ");
            attr.put("orderType",orderType);
        }
        queryResult = queryResult.concat(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
        queryResult = queryResult.concat(" ORDER BY {o:cancelledDate} DESC ");
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
        query.setResultClassList(Collections.singletonList(OrderModel.class));
        query.getQueryParameters().putAll(attr);
        parameter.setFlexibleSearchQuery(query);

        return paginatedFlexibleSearchService.search(parameter);
    }


    /**
     * @param user
     * @param store
     * @param Status
     * @param searchPageData
     * @param filter
     * @return
     */
    @Override
    public SearchPageData<OrderEntryModel> findOrderEntriesListByStatusForEPOD(UserModel user, BaseStoreModel store, List<String> Status, SearchPageData searchPageData, String filter) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        List<EpodStatus> epodStatusList = new ArrayList<>();
        for(String status: Status){
            epodStatusList.add(EpodStatus.valueOf(status));
        }
        attr.put("statusList",epodStatusList);
        attr.put("store", store);
        //attr.put(OrderModel.USER, user);
        Integer lastXDays = findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT {oe:pk} FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}");
        if(filter!=null) {
            sql.append(" join NuvocoCustomer as c on {c.pk}={o.user} ");
        }
        sql.append("} WHERE ");
        if(epodStatusList.contains(EpodStatus.PENDING)){
            sql.append(getLastXDayQuery("oe:epodInitiateDate", attr, lastXDays));
        }
        else{
            sql.append(getLastXDayQuery("oe:deliveredDate", attr, lastXDays));
        }

        sql.append(" AND {oe:epodStatus} IN (?statusList) ");
        if(filter!=null)
        {
            sql.append(" and (UPPER({o.code}) like (?filter) or UPPER({c.name}) like (?filter) or {c.uid} like (?filter) or {c.customerNo} like (?filter) or {c.mobileNumber} like (?filter) ) ");
            attr.put("filter", "%"+filter+"%");
        }
        if(epodStatusList.contains(EpodStatus.PENDING)){
            sql.append(" ORDER BY {oe.epodInitiateDate} DESC ");
        }
        else{
            sql.append(" ORDER BY {oe.deliveredDate} DESC ");
        }

        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
        query.getQueryParameters().putAll(attr);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    /**
     * @param user
     * @param store
     * @param status
     * @param searchPageData
     * @param spApprovalFilter
     * @param monthYear
     * @return
     */
    @Override
    public SearchPageData<OrderModel> findCancelOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String spApprovalFilter, String monthYear) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("statusList", Arrays.asList(status));
        Integer lastXDays = findDaysByConstraintName("CANCELLED_ORDER_VISIBLITY");
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT {o:pk} from {").append(OrderModel._TYPECODE).append(" as o} WHERE ");
        if(monthYear!=null) {
            sql.append("{o:cancelledDate} like ?monthYear");
            attr.put("monthYear", monthYear);
        }
        else {
            sql.append(getLastXDayQuery("o:cancelledDate", attr, lastXDays));
        }
        sql.append(" and {o:status} in (?statusList) ");
        sql.append(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
        sql.append(" ORDER BY {o:cancelledDate} DESC");
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Collections.singletonList(OrderModel.class));
        query.getQueryParameters().putAll(attr);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }



    @Override
    public SearchPageData<OrderEntryModel> findCancelOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String spApprovalFilter, String monthYear) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("statusList", Arrays.asList(status));
        Integer lastXDays = findDaysByConstraintName("CANCELLED_ORDER_VISIBLITY");

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT {oe:pk} FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}} WHERE ");
        if(monthYear!=null) {
            sql.append("{o:cancelledDate} like ?monthYear");
            attr.put("monthYear", monthYear);
        }
        else {
            sql.append(getLastXDayQuery("oe:cancelledDate", attr, lastXDays));
        }
        sql.append(" AND {oe:status} IN (?statusList) ");
        sql.append(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
        sql.append(" ORDER BY {oe:cancelledDate} DESC ");
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
        query.getQueryParameters().putAll(attr);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }



    @Override
    public SearchPageData<OrderEntryModel> findCancelOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter ,String productName , OrderType orderType, String spApprovalFilter, String monthYear) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("statusList", Arrays.asList(status));
        Integer lastXDays = findDaysByConstraintName("CANCELLED_ORDER_VISIBLITY");
        String queryResult=" SELECT {oe:pk} FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN NuvocoCustomer as u on {u:pk}={o:user} } WHERE ";
        if(monthYear!=null) {
            queryResult = queryResult.concat("{o:cancelledDate} LIKE ?monthYear");
            attr.put("monthYear", monthYear);
        }
        else {
            queryResult = queryResult.concat(getLastXDayQuery("o:cancelledDate", attr, lastXDays));
        }
        queryResult = queryResult.concat(" AND {o:status} IN (?statusList)");

        if(null != productName) {
            List<String> productList = Arrays.asList(productName.split(","));
            if (productList != null && !productList.isEmpty()) {
                queryResult = queryResult.concat(" and {o:productName} in (?productList) ");
                attr.put("productList", productList);
            }
        }

        if(StringUtils.isNotBlank(filter)){
            queryResult = queryResult.concat(" and (UPPER({o:code}) like ?filter OR UPPER({u:uid}) like ?filter OR UPPER({u:name}) like ?filter OR UPPER({u:customerNo}) like ?filter)");
            attr.put("filter","%"+filter.toUpperCase()+"%");
        }
        if(null!= orderType){
            queryResult = queryResult.concat(" and {o:orderType} = ?orderType ");
            attr.put("orderType",orderType);
        }
        queryResult = queryResult.concat(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
        queryResult = queryResult.concat(" ORDER BY {oe:cancelledDate} DESC ");
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
        query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
        query.getQueryParameters().putAll(attr);
        parameter.setFlexibleSearchQuery(query);

        return paginatedFlexibleSearchService.search(parameter);
    }

    private static String getLastXDayQuery(String columnName, Map<String, Object> map, Integer lastXDay) {
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
