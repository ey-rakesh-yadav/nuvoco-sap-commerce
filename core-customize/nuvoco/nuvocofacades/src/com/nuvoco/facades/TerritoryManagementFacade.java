package com.nuvoco.facades;

import com.nuvoco.facades.prosdealer.data.DealerListData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface TerritoryManagementFacade {


    SearchPageData<CustomerData> getDealerListForRetailerPagination(SearchPageData searchPageData, String filter);

    SearchPageData<CustomerData> getRetailerListForDealerPagination(SearchPageData searchPageData, String networkType,
                                                                    boolean isNew, String filter);

    Integer getDealerCountForRetailer();

    Integer getRetailerCountForDealer();

    DealerListData getRetailerListForDealer();

    DealerListData getAllDealersForSubArea();
    DealerListData getAllRetailersForSubArea();

    List<String> getAllSubAreaForCustomer(String customerId);

    DealerListData getAllRetailersForSubAreaTOP(String subArea, String dealerCode);


    List<String> getAllStatesForSO();
}
