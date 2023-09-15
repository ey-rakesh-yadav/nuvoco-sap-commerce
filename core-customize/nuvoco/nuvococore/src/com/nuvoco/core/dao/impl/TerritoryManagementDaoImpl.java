package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.TerritoryManagementDao;
import com.nuvoco.core.enums.DealerCategory;
import com.nuvoco.core.model.CustomerSubAreaMappingModel;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.SubAreaMasterModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
     * @param site
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getRetailerListForDealer(NuvocoCustomerModel nuvocoCustomer, BaseSiteModel site) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {d.retailer} from {DealerRetailerMap as d join NuvocoCustomer as c  on {d.retailer}={c.pk} } WHERE {d.dealer}=?sclCustomer AND {d.active}=?active AND {d.brand}=?brand");
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
