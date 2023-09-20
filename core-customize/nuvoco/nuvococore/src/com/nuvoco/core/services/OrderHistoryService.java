package com.nuvoco.core.services;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface OrderHistoryService {

    SearchPageData<OrderEntryModel> getTradeOrderListing(SearchPageData paginationData, String sourceType, String filter, int month, int year, String productName , String orderType, String status);
}
