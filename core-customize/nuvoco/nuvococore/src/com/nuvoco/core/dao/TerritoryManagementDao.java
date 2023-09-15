package com.nuvoco.core.dao;

import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.SubAreaMasterModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface TerritoryManagementDao {


    List<NuvocoCustomerModel> getRetailerListForDealer(NuvocoCustomerModel nuvocoCustomer, BaseSiteModel site);

    SearchPageData<NuvocoCustomerModel> getDealerListForRetailerPagination(SearchPageData searchPageData,
                                                                           NuvocoCustomerModel currentUser, BaseSiteModel currentSite, String filter);

    SearchPageData<NuvocoCustomerModel> getRetailerListForDealerPagination(SearchPageData searchPageData,
                                                                           NuvocoCustomerModel customer, BaseSiteModel site, String networkType, boolean isNew, String filter);

    Integer getDealerCountForRetailer(NuvocoCustomerModel currentUser, BaseSiteModel currentSite);

    Integer getRetailerCountForDealer(NuvocoCustomerModel currentUser, BaseSiteModel currentSite);

    List<SubAreaMasterModel> getTerritoriesForSO();
    List<NuvocoCustomerModel> getAllCustomerForTerritories(List<SubAreaMasterModel> subArea);
    List<SubAreaMasterModel> getTerritoriesForCustomer(UserModel customer);

    List<String> getAllSubAreaForCustomer(UserModel customer, BaseSiteModel site);


    SearchPageData<NuvocoCustomerModel> getCustomerByTerritoriesAndCounterType(SearchPageData searchPageData,
                                                                            List<SubAreaMasterModel> subAreaMaster, String counterType, String networkType, boolean isNew,
                                                                            String filter, String influencerType, String dealerCategory);
}
