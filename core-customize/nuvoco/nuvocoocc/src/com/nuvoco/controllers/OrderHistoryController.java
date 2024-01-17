package com.nuvoco.controllers;


import com.nuvoco.facades.OrderHistoryFacade;
import com.nuvoco.facades.data.NuvocoOrderHistoryData;
import com.nuvoco.facades.data.NuvocoOrderHistoryListData;
import com.nuvoco.occ.dto.NuvocoOrderHistoryListWsDTO;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "{baseSiteId}/users/{userId}/orderHistory")
@ApiVersion("v2")
@Tag(name = "Order History Management")
@PermitAll
public class OrderHistoryController extends  NuvocooccController{

    protected static final String HEADER_TOTAL_COUNT = "X-Total-Count";
    protected static final String DEFAULT_PAGE_SIZE = "20";
    protected static final String DEFAULT_CURRENT_PAGE = "0";
    protected static final String DEFAULT_FIELD_SET = FieldSetLevelHelper.DEFAULT_LEVEL;

    @Autowired
    private DataMapper dataMapper;

    @Autowired
    OrderHistoryFacade orderHistoryFacade;

    @RequestMapping(value="/tradeorderlisting", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getTradeOrderListing", summary = "Trade Order Listing", description = "Get list of Invoiced Orders")
    @ApiBaseSiteIdAndUserIdParam
    public NuvocoOrderHistoryListWsDTO getTradeOrderListing(@Parameter(description = "sourceType") @RequestParam(required = false) final String sourceType, @Parameter(description = "filter") @RequestParam(required = false) final String filter,
                                                            @Parameter(description = "Filters by Month") @RequestParam(required = false) Integer month,
                                                            @Parameter(description = "Status TRUCK_DISPACHED/DELIVERED") @RequestParam(required = false) String status,
                                                            @Parameter(description = "Filters by Year") @RequestParam(required = false) Integer year, final HttpServletResponse response,
                                                            @Parameter(description = "Product Name field") @RequestParam(required = false) final String productName,
                                                            @Parameter(description = "Order Type  field") @RequestParam(required = false) final String orderType,
                                                            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                            @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                            @Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize)

    {
        NuvocoOrderHistoryListData orderHistoryListData = new NuvocoOrderHistoryListData();

        int month1 = 0, year1 = 0;
        if(month!=null)
            month1 = month.intValue();
        if(year!=null)
            year1 = year.intValue();

        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

        SearchPageData<NuvocoOrderHistoryData> entries = orderHistoryFacade.getTradeOrderListing(searchPageData, sourceType, filter, month1, year1,productName,orderType,status);

        orderHistoryListData.setOrdersList(entries.getResults());

        if (entries.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(entries.getPagination().getTotalNumberOfResults()));
        }

        return dataMapper.map(orderHistoryListData, NuvocoOrderHistoryListWsDTO.class, fields);
    }

}
