package com.nuvoco.facades;

import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.facades.data.DealerCurrentNetworkData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface NuvocoNetworkFacade {

    SearchPageData<DealerCurrentNetworkData> getRetailerDetailedSummaryList(String searchKey, Boolean isNew,
                                                                            String networkType, SearchPageData searchPageData);
    List<DealerCurrentNetworkData> getRetailerDetailedSummaryListData(List<NuvocoCustomerModel> retailerList) ;
}
