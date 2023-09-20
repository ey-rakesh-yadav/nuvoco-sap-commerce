package com.nuvoco.core.services;

import com.nuvoco.core.model.CustDepotMasterModel;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.NuvocoUserModel;
import com.nuvoco.core.model.SubAreaMasterModel;
import com.nuvoco.facades.data.RequestCustomerData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface TerritoryManagementService {

    List<NuvocoCustomerModel> getCustomerforUser(RequestCustomerData requestCustomerData);

    List<NuvocoCustomerModel> getRetailerListForDealer();

    List<SubAreaMasterModel> getTerritoriesForCustomer(String customerId);

    List<String> getAllSubAreaForCustomer(String customerId);

    SearchPageData<NuvocoCustomerModel> getDealerListForRetailerPagination(SearchPageData searchPageData, String filter);

    SearchPageData<NuvocoCustomerModel> getRetailerListForDealerPagination(SearchPageData searchPageData,
                                                                        String networkType, boolean isNew, String filter);

    SearchPageData<NuvocoCustomerModel> getCustomerByTerritoriesAndCounterType(SearchPageData searchPageData,
                                                                            String counterType, String networkType, boolean isNew, String filter, String influencerType,
                                                                            String dealerCategory);

    SearchPageData<NuvocoCustomerModel> getCustomerByTerritoriesAndCounterType(SearchPageData searchPageData,
                                                                            List<SubAreaMasterModel> subAreaMaster, String counterType, String networkType, boolean isNew,
                                                                            String filter, String influencerType, String dealerCategory);

    CustDepotMasterModel getCustDepotForCustomer(NuvocoCustomerModel customer);

    Integer getDealerCountForRetailer();

    Integer getRetailerCountForDealer();

    NuvocoUserModel getSOforCustomer(NuvocoCustomerModel customer);

    List<NuvocoCustomerModel> getDealersForSubArea();
    List<NuvocoCustomerModel> getRetailersForSubArea();

    List<SubAreaMasterModel> getTerritoriesForCustomer(NuvocoCustomerModel customer);

    List<NuvocoCustomerModel> getAllCustomerForSubArea(List<SubAreaMasterModel> subAreas);

    List<SubAreaMasterModel> getTerritoriesForSO();

    List<SubAreaMasterModel> getTerritoriesForSO(String uid);

    List<NuvocoCustomerModel> getAllRetailersForSubAreaTOP(String subArea, String dealerCode);

    List<String> getAllStatesForSO();

    SubAreaMasterModel getTerritoryById(String territoryId);

}
