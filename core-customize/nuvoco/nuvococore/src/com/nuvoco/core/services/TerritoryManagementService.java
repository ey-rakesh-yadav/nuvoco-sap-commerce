package com.nuvoco.core.services;

import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.SubAreaMasterModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface TerritoryManagementService {

    List<NuvocoCustomerModel> getRetailerListForDealer();

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

    Integer getDealerCountForRetailer();

    Integer getRetailerCountForDealer();

    List<NuvocoCustomerModel> getDealersForSubArea();
    List<NuvocoCustomerModel> getRetailersForSubArea();

    List<SubAreaMasterModel> getTerritoriesForCustomer(NuvocoCustomerModel customer);

    List<NuvocoCustomerModel> getAllCustomerForSubArea(List<SubAreaMasterModel> subAreas);

    List<SubAreaMasterModel> getTerritoriesForSO();

}
