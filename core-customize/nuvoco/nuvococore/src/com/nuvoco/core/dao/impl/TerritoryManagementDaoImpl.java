package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.TerritoryManagementDao;
import com.nuvoco.core.enums.CounterType;
import com.nuvoco.core.enums.DealerCategory;
import com.nuvoco.core.enums.NuvocoUserType;
import com.nuvoco.core.model.*;
import com.nuvoco.facades.data.FilterTalukaData;
import com.nuvoco.facades.data.RequestCustomerData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;

public class TerritoryManagementDaoImpl implements TerritoryManagementDao {

    @Autowired
    FlexibleSearchService flexibleSearchService;

    @Autowired
    PaginatedFlexibleSearchService paginatedFlexibleSearchService;

    @Resource
    private B2BUnitService b2bUnitService;

    @Autowired
    BaseSiteService baseSiteService;
    @Autowired
    UserService userService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SearchRestrictionService searchRestrictionService;

    /**
     * @param nuvocoCustomer
     * @return
     */
    @Override
    public NuvocoUserModel getSOForSubArea(NuvocoCustomerModel nuvocoCustomer) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder("SELECT {u:nuvocoUser} FROM {CustomerSubAreaMapping as c JOIN UserSubAreaMapping as u on {u:subAreaMaster}={c:subAreaMaster} and {u:brand}={c:brand} } WHERE {c.nuvocoCustomer} =?nuvocoCustomer and {u.isActive}=?active");
        boolean active = Boolean.TRUE;
        params.put("nuvocoCustomer", nuvocoCustomer);
        params.put("active", active);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(NuvocoUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<NuvocoUserModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() != null && !searchResult.getResult().isEmpty() ? searchResult.getResult().get(0) : null;

    }

    /**
     * @param requestCustomerData
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getDealersForSP(RequestCustomerData requestCustomerData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        String strQuery = getDealersForSPQuery(requestCustomerData, params);

        FlexibleSearchQuery query = new FlexibleSearchQuery(strQuery);
        query.setResultClassList(Collections.singletonList(NuvocoCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<NuvocoCustomerModel> searchResult = flexibleSearchService.search(query);
        List<NuvocoCustomerModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }



    private String getDealersForSPQuery(RequestCustomerData requestCustomerData, Map<String, Object> params) {
        final StringBuilder builder = new StringBuilder("Select {c.pk} from {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode} join NuvocoCustomer as c on {c.pk}={d.dealerCode} }")
                .append(" where {s.spCode} = ?currentUser AND {s.brand} = ?brand AND {d.brand}= ?brand AND {s.active} = ?active ");
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("currentUser", userService.getCurrentUser());
        requestCustomerData.setIncludeNonSclCustomer(true);
        appendFilterQuery(builder, params, requestCustomerData);
        return builder.toString();
    }

    /**
     * @param requestCustomerData
     * @param subAreaMasterList
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getCustomerForUser(RequestCustomerData requestCustomerData, List<SubAreaMasterModel> subAreaMasterList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        String strQuery = getCustomerForUserQuery(requestCustomerData, subAreaMasterList, params);
        FlexibleSearchQuery query = new FlexibleSearchQuery(strQuery);
        query.setResultClassList(Collections.singletonList(NuvocoCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<NuvocoCustomerModel> searchResult = flexibleSearchService.search(query);
        List<NuvocoCustomerModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    /**
     * @param currentUser
     * @param currentSite
     * @return
     */
    @Override
    public Integer getInfluencerCountForDealer(NuvocoCustomerModel currentUser, BaseSiteModel currentSite) {
        final Map<String, Object> attr = new HashMap<>();
        boolean active = Boolean.TRUE;
        attr.put("currentUser", currentUser);
        attr.put("currentSite", currentSite);
        attr.put("active", active);

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT{d.influencer}) FROM {DealerInfluencerMap as d} WHERE {d.fromCustomer}=?currentUser AND {d.brand}=?currentSite AND {d.active}=?active AND {d.fromCustomerType}= 'Dealer' ");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<Integer> result = flexibleSearchService.search(query);

        return result.getResult().get(0);
    }


    private String getCustomerForUserQuery(RequestCustomerData requestCustomerData, List<SubAreaMasterModel> subAreaMasterList, Map<String, Object> params) {
        final StringBuilder builder = new StringBuilder("SELECT {c.pk} FROM {CustomerSubAreaMapping as m join NuvocoCustomer as c on {m.nuvocoCustomer}={c.pk} } WHERE {m.subAreaMaster} in (?subAreaList) ");
        params.put("subAreaList", subAreaMasterList);
        appendFilterQuery(builder, params, requestCustomerData);
        return builder.toString();
    }


    public void appendFilterQuery(StringBuilder builder, Map<String, Object> params, RequestCustomerData requestCustomerData) {
        if (requestCustomerData.getCounterType() != null && !requestCustomerData.getCounterType().isEmpty()) {
            List<CounterType> counterTypes = new ArrayList<CounterType>();
            for (String counterType : requestCustomerData.getCounterType()) {
                if (StringUtils.isNotBlank(counterType) && CounterType.valueOf(counterType) != null) {
                    counterTypes.add(CounterType.valueOf(counterType));
                }
            }
            if (!counterTypes.isEmpty()) {
                builder.append("and {c.counterType} in (?counterTypes) ");
                params.put("counterTypes", counterTypes);
            }
        }
        if (StringUtils.isNotBlank(requestCustomerData.getNetworkType())) {
            builder.append("and {c.networkType}=?networkType ");
            params.put("networkType", requestCustomerData.getNetworkType());
        }
        if (StringUtils.isNotBlank(requestCustomerData.getSearchKey())) {
            builder.append("and (UPPER({c.name}) like (?filter) or {c.uid} like (?filter) or {c.customerNo} like (?filter) or {c.mobileNumber} like (?filter) ) ");
            params.put("filter", "%" + requestCustomerData.getSearchKey() + "%");
        }
       /* if (StringUtils.isNotBlank(requestCustomerData.getInfluencerType())) {
            builder.append("and {c.influencerType}=?influencerType ");
            params.put("influencerType", InfluencerType.valueOf(requestCustomerData.getInfluencerType()));
        }*/
        if (StringUtils.isNotBlank(requestCustomerData.getCustomerUid())) {
            builder.append("and {c.uid}=?uid ");
            params.put("uid", requestCustomerData.getCustomerUid());
        }
        if (requestCustomerData.getRemoveFlaggedCustomer() != null && requestCustomerData.getRemoveFlaggedCustomer()) {
            builder.append("and {c.isDealerFlag}=?isDealerFlag ");
            params.put("isDealerFlag", Boolean.FALSE);
        }
        if (Objects.nonNull(requestCustomerData.getDealerCategory())) {
            builder.append("and {c.dealerCategory}=?dealerCategory ");
            params.put("dealerCategory", DealerCategory.valueOf(requestCustomerData.getDealerCategory()));
        }
        if (requestCustomerData.getIsNew() != null && requestCustomerData.getIsNew()) {
            LocalDate lastNinetyDay = LocalDate.now().minusDays(90);
            builder.append("and {c.dateOfJoining}>=?lastNinetyDay ");
            params.put("lastNinetyDay", lastNinetyDay.toString());
        }
      /*  if (requestCustomerData.getIncludeNonCustomer() == null || !requestCustomerData.getIncludeNonSclCustomer()) {
            builder.append("and {m.isOtherBrand}=?isOtherBrand ");
            params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
        }*/
        if (requestCustomerData.getIsFlag() != null && requestCustomerData.getIsFlag().equals(Boolean.TRUE)) {
            builder.append("and {c.isDealerFlag}=?isDealerFlag ");
            params.put("isDealerFlag", Boolean.TRUE);
        }
        if (requestCustomerData.getIsUnFlag() != null && requestCustomerData.getIsUnFlag().equals(Boolean.TRUE)) {
            builder.append("and {c.isUnFlagRequestRaised}=?isUnFlagRequestRaised ");
            params.put("isUnFlagRequestRaised", Boolean.TRUE);
        }
    }
    /**
     * @param customer
     * @return
     */
    @Override
    public CustDepotMasterModel getCustDepotForCustomer(NuvocoCustomerModel customer) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {custDepotCode} FROM {CustDepotDealerMapping} WHERE {dealerCode} = ?customer AND {active} = ?active AND {brand} = ?brand ");
        params.put("customer", customer);
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(CustDepotMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<CustDepotMasterModel> searchResult = flexibleSearchService.search(query);
        List<CustDepotMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

    /**
     * @param nuvocoCustomer
     * @param site
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getRetailerListForDealer(NuvocoCustomerModel nuvocoCustomer, BaseSiteModel site) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {d.retailer} from {DealerRetailerMap as d join NuvocoCustomer as c  on {d.retailer}={c.pk} } WHERE {d.dealer}=?nuvocoCustomer AND {d.active}=?active AND {d.brand}=?brand");
        boolean active = Boolean.TRUE;
        params.put("nuvocoCustomer", nuvocoCustomer);
        params.put("active", active);
        params.put("brand", site);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(NuvocoCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<NuvocoCustomerModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult();
    }

    /**
     * @param searchPageData
     * @param currentUser
     * @param currentSite
     * @param filter
     * @return
     */
    @Override
    public SearchPageData<NuvocoCustomerModel> getDealerListForRetailerPagination(SearchPageData searchPageData, NuvocoCustomerModel currentUser, BaseSiteModel currentSite, String filter) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select DISTINCT {d.dealer} from {DealerRetailerMap as d join NuvocoCustomer as c on {d.dealer}={c.pk} } where {d.retailer}=?currentUser AND {d.active}=?active AND {d.brand}=?currentSite ");
        boolean active = Boolean.TRUE;
        attr.put("currentUser", currentUser);
        attr.put("active", active);
        attr.put("currentSite", currentSite);
        appendFilterQuery(builder, attr, null, false, filter, null, null);
        builder.append(" order by {d.dealer}");
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(NuvocoCustomerModel.class));
        query.getQueryParameters().putAll(attr);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    /**
     * @param searchPageData
     * @param customer
     * @param site
     * @param networkType
     * @param isNew
     * @param filter
     * @return
     */
    @Override
    public SearchPageData<NuvocoCustomerModel> getRetailerListForDealerPagination(SearchPageData searchPageData, NuvocoCustomerModel customer, BaseSiteModel site, String networkType, boolean isNew, String filter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {d.retailer} from {DealerRetailerMap as d join NuvocoCustomer as c  on {d.retailer}={c.pk} } WHERE {d.dealer}=?customer AND {d.active}=?active AND {d.brand}=?brand");
        boolean active = Boolean.TRUE;
        params.put("customer", customer);
        params.put("active", active);
        params.put("brand", site);
        appendFilterQuery(builder, params, networkType, isNew, filter, null, null);
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(NuvocoCustomerModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }



    /**
     * @param currentUser
     * @param currentSite
     * @return
     */
    @Override
    public Integer getDealerCountForRetailer(NuvocoCustomerModel currentUser, BaseSiteModel currentSite) {
        final Map<String, Object> attr = new HashMap<>();
        boolean active = Boolean.TRUE;
        attr.put("currentUser", currentUser);
        attr.put("currentSite", currentSite);
        attr.put("active", active);

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT{d.dealer}) FROM {DealerRetailerMap as d} WHERE {d.retailer}=?currentUser AND {d.brand}=?currentSite AND {d.active}=?active");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<Integer> result = flexibleSearchService.search(query);

        return result.getResult().get(0);
    }

    /**
     * @param currentUser
     * @param currentSite
     * @return
     */
    @Override
    public Integer getRetailerCountForDealer(NuvocoCustomerModel currentUser, BaseSiteModel currentSite) {
        final Map<String, Object> attr = new HashMap<>();
        boolean active = Boolean.TRUE;
        attr.put("currentUser", currentUser);
        attr.put("currentSite", currentSite);
        attr.put("active", active);

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT{d.retailer}) FROM {DealerRetailerMap as d} WHERE {d.dealer}=?currentUser AND {d.brand}=?currentSite AND {d.active}=?active");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<Integer> result = flexibleSearchService.search(query);

        return result.getResult().get(0);
    }


    @Override
    public List<SubAreaMasterModel> getTerritoriesForSO(NuvocoUserModel user) {
        final Map<String, Object> params = new HashMap<>();
        String builder = "SELECT {subAreaMaster} FROM {UserSubAreaMapping} WHERE {nuvocoUser} = ?user AND {isActive} = 1 AND {brand} = ?brand";
        params.put("user", user);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
        query.setResultClassList(List.of(SubAreaMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        List<SubAreaMasterModel> result = searchResult.getResult();
        if (CollectionUtils.isNotEmpty(result)) {
            return result;
        }
        return Collections.emptyList();
    }
    /**
     * @return
     */
    @Override
    public List<SubAreaMasterModel> getTerritoriesForSO() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {subAreaMaster} FROM {UserSubAreaMapping} WHERE {nuvocoUser} = ?nuvocoUser AND {isActive} = ?active AND {brand} = ?brand");
        params.put("nuvocoUser", userService.getCurrentUser());
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(SubAreaMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        List<SubAreaMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    /**
     * @param subArea
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getAllCustomerForTerritories(List<SubAreaMasterModel> subArea) {
        return (List<NuvocoCustomerModel>) sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public List<NuvocoCustomerModel> execute() {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    final Map<String, Object> params = new HashMap<String, Object>();
                    final StringBuilder builder = new StringBuilder("SELECT {nuvocoCustomer} FROM {CustomerSubAreaMapping} WHERE {subAreaMaster} in (?subAreaMaster) AND {isActive} = ?active AND {brand} = ?brand and {nuvocoCustomer} is not null ");
                    params.put("subAreaMaster", subArea);
                    params.put("active", Boolean.TRUE);
                    params.put("brand", baseSiteService.getCurrentBaseSite());
//    				params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
                    FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
                    query.setResultClassList(Collections.singletonList(NuvocoCustomerModel.class));
                    query.addQueryParameters(params);
                    final SearchResult<NuvocoCustomerModel> searchResult = flexibleSearchService.search(query);
                    List<NuvocoCustomerModel> result = searchResult.getResult();
                    return result != null && !result.isEmpty() ? result : Collections.emptyList();
                } finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    /**
     * @param customer
     * @return
     */
    @Override
    public List<SubAreaMasterModel> getTerritoriesForCustomer(UserModel customer) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {subAreaMaster} FROM {CustomerSubAreaMapping} WHERE {nuvocoCustomer} = ?customer AND {isActive} = ?active AND {brand} = ?brand and {nuvocoCustomer} is not null and {isOtherBrand}=?isOtherBrand ");
        params.put("customer", customer);
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(SubAreaMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        List<SubAreaMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    /**
     * @param customer
     * @param site
     * @return
     */
    @Override
    public List<String> getAllSubAreaForCustomer(UserModel customer, BaseSiteModel site) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {subArea} FROM {CustomerSubAreaMapping} WHERE {nuvocoCustomer} = ?customer AND {isActive} = ?active AND {brand} = ?brand");
        boolean active = Boolean.TRUE;
        params.put("customer", customer);
        params.put("active", active);
        params.put("brand", site);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(String.class));
        query.addQueryParameters(params);
        final SearchResult<String> searchResult = flexibleSearchService.search(query);
        List<String> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    /**
     * @param searchPageData
     * @param subAreaMaster
     * @param counterType
     * @param networkType
     * @param isNew
     * @param filter
     * @param influencerType
     * @param dealerCategory
     * @return
     */
    @Override
    public SearchPageData<NuvocoCustomerModel> getCustomerByTerritoriesAndCounterType(SearchPageData searchPageData, List<SubAreaMasterModel> subAreaMaster, String counterType, String networkType, boolean isNew, String filter, String influencerType, String dealerCategory) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {m.nuvocoCustomer} FROM {CustomerSubAreaMapping as m join NuvocoCustomer as c on {c.pk}={m.nuvocoCustomer} } WHERE {m.subAreaMaster} in (?subAreaMaster) AND {m.counterType}=?counterType AND {m.isActive} = ?active AND {m.brand} = ?brand and {m.nuvocoCustomer} is not null and {m.isOtherBrand}=?isOtherBrand ");
        boolean active = Boolean.TRUE;
        params.put("subAreaMaster", subAreaMaster);
        params.put("active", active);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("counterType", counterType);
        params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);

        appendFilterQuery(builder, params, networkType, isNew, filter, influencerType, dealerCategory);
        builder.append(" order by {c.uid} ");

        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(NuvocoCustomerModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    /**
     * @param territoryId
     * @return
     */
    @Override
    public SubAreaMasterModel getTerritoryById(String territoryId) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {SubAreaMaster} WHERE {pk} = ?territoryId ";
        params.put("territoryId", territoryId);

        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    /**
     * @param subAreas
     * @param site
     * @param dealerCode
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getAllRetailersForSubAreaTOP(List<SubAreaMasterModel> subAreas, BaseSiteModel site, String dealerCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {cs.nuvocoCustomer} from {CustomerSubAreaMapping as cs left join DealerRetailerMap as dr on {cs.nuvocoCustomer}={dr.retailer} and {dr.dealer}=?dealer} where {cs.subAreaMaster} in (?subAreas) AND {isActive} = ?active AND {brand} = ?brand and {cs.nuvocoCustomer} is not null and {cs.isOtherBrand}=?isOtherBrand order by {dr.orderCount} desc");
        boolean active = Boolean.TRUE;
        params.put("subAreas", subAreas);
        params.put("active", active);
        params.put("brand", site);
        params.put("dealer", userService.getUserForUID(dealerCode));
        params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(NuvocoCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<NuvocoCustomerModel> searchResult = flexibleSearchService.search(query);
        List<NuvocoCustomerModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    /**
     * @param filterTalukaData
     * @return
     */
    @Override
    public List<SubAreaMasterModel> getTalukaForUser(FilterTalukaData filterTalukaData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        NuvocoUserModel currentUser = (NuvocoUserModel) userService.getCurrentUser();
        final StringBuilder builder = new StringBuilder();

        if (currentUser instanceof NuvocoUserModel) {
            if (currentUser.getUserType() != null) {
                if (currentUser.getUserType().getCode().equals("SO")) {
                    builder.append("select {s:pk} from {UserSubAreaMapping as u join SubAreaMaster as s on {u:subAreaMaster}={s:pk}}" +
                            " where {u:nuvocoUser} = ?nuvocoUser and {u:brand} = ?brand and {u:isActive} = ?active");
                    params.put("nuvocoUser", currentUser);
                } else if (currentUser.getUserType().getCode().equals("TSM")) {
                    builder.append("select {s:pk} from {TsmDistrictMapping as tsm join DistrictMaster as d on {tsm:district}={d:pk}" +
                            " join SubAreaMaster as s on {d:name}={s:district}} where {tsm:tsmUser} = ?tsmUser and {tsm:brand} = ?brand" +
                            " and {tsm:isActive} = ?active");
                    params.put("tsmUser", currentUser);
                    if (filterTalukaData != null && !ObjectUtils.isEmpty(filterTalukaData) && filterTalukaData.getDistrictCode() != null && !filterTalukaData.getDistrictCode().isEmpty()) {
                        builder.append(" and {d:code} = ?districtCode");
                        params.put("districtCode", filterTalukaData.getDistrictCode());
                    }
                } else if (currentUser.getUserType().getCode().equals("RH")) {
                    builder.append("select {s:pk} from {RhRegionMapping as rh join RegionMaster as r on {rh:region}={r:pk}" +
                            " join DistrictMaster as d on {d:region}={r:pk} join SubAreaMaster as s on {d:name}={s:district}}" +
                            " where {rh:rhUser} = ?rhUser and {rh:brand} = ?brand and {rh:isActive} = ?active");
                    params.put("rhUser", currentUser);
                    if (filterTalukaData != null && !ObjectUtils.isEmpty(filterTalukaData) && filterTalukaData.getRegionCode() != null && !filterTalukaData.getRegionCode().isEmpty()) {
                        builder.append(" and {r:code} = ?regionCode");
                        params.put("regionCode", filterTalukaData.getRegionCode());
                        if (filterTalukaData.getDistrictCode() != null && !ObjectUtils.isEmpty(filterTalukaData) && !filterTalukaData.getDistrictCode().isEmpty()) {
                            builder.append(" and {d:code} = ?districtCode");
                            params.put("districtCode", filterTalukaData.getDistrictCode());
                        }
                    }
                } else if (currentUser.getUserType().equals(NuvocoUserType.TSO)) {
                    builder.append("select {s:pk} from {TsoTalukaMapping as t join SubAreaMaster as s on {t:subAreaMaster}={s:pk}}" +
                            " where {t:tsoUser} = ?nuvocoUser and {t:brand} = ?brand and {t:isActive} = ?active");
                    params.put("nuvocoUser", currentUser);

                }

                if (filterTalukaData != null && !ObjectUtils.isEmpty(filterTalukaData) && filterTalukaData.getTalukaName() != null && !filterTalukaData.getTalukaName().isEmpty()) {
                    builder.append(" and {s:taluka} like ?talukaName");
                    params.put("talukaName", filterTalukaData.getTalukaName().toUpperCase() + "%");
                }
                builder.append(" order by {s:pk}");
                params.put("active", Boolean.TRUE);
                params.put("brand", baseSiteService.getCurrentBaseSite());

            } else {
                throw new UnknownIdentifierException("User Type not set for the user");
            }

        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SubAreaMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * @param filterTalukaData
     * @return
     */
    @Override
    public List<SubAreaMasterModel> getTalukaForSP(FilterTalukaData filterTalukaData) {
       /* final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select distinct{c.subAreaMaster} from {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode} join CustomerSubAreaMapping as c on {c.sclCustomer}={d.dealerCode} } Where ")
                .append(" {s.spCode} = ?currentUser AND {s.brand} = ?brand AND {d.brand}= ?brand AND {s.active} = ?active ");
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("currentUser", userService.getCurrentUser());
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SubAreaMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        List<SubAreaMasterModel> result = searchResult.getResult();*/
        //return result != null && !result.isEmpty() ? result : Collections.emptyList();
        return Collections.emptyList();
    }

    /**
     * @param nuvocoUser
     * @param site
     * @return
     */
    @Override
    public List<String> getAllStatesForSO(UserModel nuvocoUser, BaseSiteModel site) {
        List<String> result = new ArrayList<String>();
        result.add(((B2BCustomerModel) nuvocoUser).getState());
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    private void appendFilterQuery(StringBuilder builder, Map<String, Object> params, String networkType, boolean isNew, String filter, String influencerType, String dealerCategory) {
        if (StringUtils.isNotBlank(networkType)) {
            builder.append(" and {c.networkType}=?networkType ");
            params.put("networkType", networkType);
        }
        if (StringUtils.isNotBlank(filter)) {
            builder.append(" and (UPPER({c.name}) like (?filter) or {c.uid} like (?filter) or {c.customerNo} like (?filter) or {c.mobileNumber} like (?filter) )");
            params.put("filter", "%" + filter.toUpperCase() + "%");
        }

        if (Objects.nonNull(dealerCategory)) {
            builder.append(" and {c.dealerCategory}=?dealerCategory ");
            params.put("dealerCategory", DealerCategory.valueOf(dealerCategory));
        }
        if (isNew) {
            LocalDate lastNinetyDay = LocalDate.now().minusDays(90);
            builder.append(" and {c.dateOfJoining}>=?lastNinetyDay ");
            params.put("lastNinetyDay", lastNinetyDay.toString());
        }
    }
}
