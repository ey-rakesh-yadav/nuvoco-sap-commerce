package com.nuvoco.facades;

import com.nuvoco.facades.data.NuvocoOrderHistoryData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface OrderHistoryFacade {

    SearchPageData<NuvocoOrderHistoryData> getTradeOrderListing(SearchPageData paginationData, String sourceType, String filter, int month, int year, String productName, String orderType, String status);
}
