package com.nuvoco.facades.impl;

import com.nuvoco.core.services.NuvocoOrderHistoryService;
import com.nuvoco.facades.OrderHistoryFacade;
import com.nuvoco.facades.data.NuvocoOrderHistoryData;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class OrderHistoryFacadeImpl implements OrderHistoryFacade {

    @Autowired
    NuvocoOrderHistoryService nuvocoOrderHistoryService;


    @Autowired
    Converter<OrderEntryModel, NuvocoOrderHistoryData> nuvocoOrderEntryHistoryCardConverter;
    /**
     * @param paginationData
     * @param sourceType
     * @param filter
     * @param month
     * @param year
     * @param productName
     * @param orderType
     * @param status
     * @return
     */
    @Override
    public SearchPageData<NuvocoOrderHistoryData> getTradeOrderListing(SearchPageData paginationData, String sourceType, String filter, int month, int year, String productName, String orderType, String status) {
        SearchPageData<OrderEntryModel> entries = nuvocoOrderHistoryService.getTradeOrderListing(paginationData, sourceType, filter, month, year,productName,orderType, status);
        final SearchPageData<NuvocoOrderHistoryData> result = new SearchPageData<>();
        result.setPagination(entries.getPagination());
        result.setSorts(entries.getSorts());
        List<NuvocoOrderHistoryData> sclOrderHistoryData = nuvocoOrderEntryHistoryCardConverter.convertAll(entries.getResults());
        result.setResults(sclOrderHistoryData);
        return result;
    }
}
