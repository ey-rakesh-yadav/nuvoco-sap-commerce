package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.DJPVisitDao;
import com.nuvoco.core.model.*;
import com.nuvoco.core.utility.NuvocoDateUtility;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;

public class DJPVisitDaoImpl implements DJPVisitDao {

    private static final Logger LOG = Logger.getLogger(DJPVisitDaoImpl.class);

    private static final String VISIT_FOR_DAY = "SELECT {vm.pk} FROM {VisitMaster AS vm} WHERE {vm.user}=?currentUser AND  ";

    @Resource
    private PaginatedFlexibleSearchService paginatedFlexibleSearchService;

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    private SessionService sessionService;


    /**
     * @param objectiveId
     * @return
     */
    @Override
    public ObjectiveModel findOjectiveById(String objectiveId) {
        ObjectiveModel model = null;
        if(objectiveId!=null) {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {Objective} WHERE {objectiveId}=?objectiveId ");
            params.put("objectiveId", objectiveId);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Arrays.asList(ObjectiveModel.class));
            query.addQueryParameters(params);
            final SearchResult<ObjectiveModel> searchResult = flexibleSearchService.search(query);
            if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
                model = searchResult.getResult().get(0);
        }
        return model;
    }

    /**
     * @param djpRun
     * @return
     */
    @Override
    public List<DJPRouteScoreMasterModel> findAllRouteForPlannedDate(DJPRunMasterModel djpRun) {
        List<DJPRouteScoreMasterModel> modelList = new ArrayList<DJPRouteScoreMasterModel>();
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {dr.pk} FROM {DJPRouteScoreMaster as dr} WHERE {dr.run}=?run order by {dr.routesScore} ");
        params.put("run", djpRun);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(DJPRouteScoreMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<DJPRouteScoreMasterModel> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            modelList = searchResult.getResult();
        return modelList;
    }

    /**
     * @param nuvocoCustomer
     * @param month
     * @param year
     * @return
     */
    @Override
    public Integer getVisitCountMTD(NuvocoCustomerModel nuvocoCustomer, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT COUNT({endVisitTime}) FROM {CounterVisitMaster AS c} WHERE {c.nuvocoCustomer}=?nuvocoCustomer AND {c.endVisitTime} IS NOT NULL AND ").append(NuvocoDateUtility.getDateClauseQueryByMonthYear("c.endVisitTime", month, year, params));
        params.put("nuvocoCustomer", nuvocoCustomer);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.addQueryParameters(params);
        final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0;
        else
            return 0;
    }

    /**
     * @param userId
     * @param month
     * @param year
     * @return
     */
    @Override
    public List<List<Object>> getCounterSharesForDealerOrRetailer(String userId, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT SUM({c.counterShare}), COUNT({c.pk}) FROM {CounterVisitMaster AS c} WHERE {c.nuvocoCustomer}=?userId AND ").append(NuvocoDateUtility.getDateClauseQueryByMonthYear("c.endVisitTime", month+1, year, params));
        params.put("userId", userId);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class,Integer.class));
        query.addQueryParameters(params);
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);

        return searchResult.getResult();
    }

    /**
     * @param customer
     * @return
     */
    @Override
    public Double getDealerOutstandingAmount(String customer) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {totalOutstanding} FROM {CreditAndOutstanding} WHERE {customerCode}=?customer");
        params.put("customer", customer);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    /**
     * @param customer
     * @return
     */
    @Override
    public Double getDealerCreditLimit(String customer) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {creditLimit} FROM {CreditAndOutstanding} WHERE {customerCode}=?customer");
        params.put("customer", customer);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    /**
     * @param customerNo
     * @param brand
     * @return
     */
    @Override
    public Date getLastLiftingDateForDealer(String customerNo, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select MAX({invoiceDate}) from {SalesHistory} where {customerNo}=?customerNo AND {brand}=?brand");
        params.put("customerNo", customerNo);
        params.put("brand", brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Date.class));
        query.addQueryParameters(params);
        final SearchResult<Date> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult().get(0);
    }

    /**
     * @param customer
     * @return
     */
    @Override
    public List<List<Double>> getOutstandingBucketsForDealer(String customer) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {bucket1},{bucket2},{bucket3},{bucket4},{bucket5},{bucket6},{bucket7},{bucket8},{bucket9},{bucket10} FROM {CreditAndOutstanding} WHERE {customerCode}=?customer");
        params.put("customer", customer);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class,Double.class,Double.class,Double.class,Double.class,Double.class,Double.class,Double.class,Double.class,Double.class));
        query.addQueryParameters(params);
        final SearchResult<List<Double>> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult();

    }

    /**
     * @param user
     * @param plannedDate
     * @return
     */
    @Override
    public List<VisitMasterModel> getPlannedVisitForToday(UserModel user, String plannedDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("currentUser", user);

        final StringBuilder builder = new StringBuilder(VISIT_FOR_DAY).append(NuvocoDateUtility.getDateRangeClauseQuery("vm.visitPlannedDate", plannedDate, plannedDate, params));

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
        final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult();
    }

    /**
     * @param customerNo
     * @param brand
     * @param maxDate
     * @return
     */
    @Override
    public Double getLastLiftingQuantityForDealer(String customerNo, BaseSiteModel brand, Date maxDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder sql = new StringBuilder("SELECT SUM({quantity}) FROM {SalesHistory} WHERE {customerNo}=?customerNo AND {brand}=?brand AND {invoiceDate}=?maxDate");
        params.put("customerNo", customerNo);
        params.put("brand", brand);
        params.put("maxDate", maxDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }
}
