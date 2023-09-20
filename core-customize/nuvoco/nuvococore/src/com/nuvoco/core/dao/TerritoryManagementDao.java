package com.nuvoco.core.dao;

import com.nuvoco.core.model.CustDepotMasterModel;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.NuvocoUserModel;
import com.nuvoco.core.model.SubAreaMasterModel;
import com.nuvoco.facades.data.FilterTalukaData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface TerritoryManagementDao {

    NuvocoUserModel getSOForSubArea(NuvocoCustomerModel sclCustomer);

    CustDepotMasterModel getCustDepotForCustomer(NuvocoCustomerModel customer);
    List<NuvocoCustomerModel> getRetailerListForDealer(NuvocoCustomerModel nuvocoCustomer, BaseSiteModel site);

    SearchPageData<NuvocoCustomerModel> getDealerListForRetailerPagination(SearchPageData searchPageData,
                                                                           NuvocoCustomerModel currentUser, BaseSiteModel currentSite, String filter);

    SearchPageData<NuvocoCustomerModel> getRetailerListForDealerPagination(SearchPageData searchPageData,
                                                                           NuvocoCustomerModel customer, BaseSiteModel site, String networkType, boolean isNew, String filter);

    Integer getDealerCountForRetailer(NuvocoCustomerModel currentUser, BaseSiteModel currentSite);

    Integer getRetailerCountForDealer(NuvocoCustomerModel currentUser, BaseSiteModel currentSite);

    List<SubAreaMasterModel> getTerritoriesForSO();

    List<SubAreaMasterModel> getTerritoriesForSO(NuvocoUserModel user);
    List<NuvocoCustomerModel> getAllCustomerForTerritories(List<SubAreaMasterModel> subArea);
    List<SubAreaMasterModel> getTerritoriesForCustomer(UserModel customer);

    List<String> getAllSubAreaForCustomer(UserModel customer, BaseSiteModel site);


    SearchPageData<NuvocoCustomerModel> getCustomerByTerritoriesAndCounterType(SearchPageData searchPageData,
                                                                            List<SubAreaMasterModel> subAreaMaster, String counterType, String networkType, boolean isNew,
                                                                            String filter, String influencerType, String dealerCategory);

    SubAreaMasterModel getTerritoryById(String territoryId);

    List<NuvocoCustomerModel> getAllRetailersForSubAreaTOP(List<SubAreaMasterModel> subAreas, BaseSiteModel site,
                                                        String dealerCode);


    List<SubAreaMasterModel> getTalukaForUser(FilterTalukaData filterTalukaData);

    List<SubAreaMasterModel> getTalukaForSP(FilterTalukaData filterTalukaData);

    List<String> getAllStatesForSO(UserModel sclUser, BaseSiteModel site);





}
