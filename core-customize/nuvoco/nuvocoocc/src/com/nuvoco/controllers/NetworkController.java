package com.nuvoco.controllers;


import com.nuvoco.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.nuvoco.facades.NuvocoNetworkFacade;
import com.nuvoco.facades.data.DealerCurrentNetworkData;
import com.nuvoco.facades.data.DealerCurrentNetworkListData;
import com.nuvoco.occ.dto.DealerCurrentNetworkListDto;
import com.nuvoco.security.NuvocoSecuredAccessConstants;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/{baseSiteId}/network")
@ApiVersion("v2")
@Tag(name = "Nuvoco Network Management")
public class NetworkController {

    private static final Logger LOGGER = Logger.getLogger(NetworkController.class);
    protected static final String DEFAULT_PAGE_SIZE = "20";
    protected static final String DEFAULT_CURRENT_PAGE = "0";

    protected static final String HEADER_TOTAL_COUNT = "X-Total-Count";
    private static final String DEFAULT_FIELD_SET = FieldSetLevelHelper.DEFAULT_LEVEL;

   @Autowired
    DataMapper dataMapper;
   @Autowired
    NuvocoNetworkFacade nuvocoNetworkFacade;

    @Secured({NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT, NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP, NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/" + "retailerDetailList", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(operationId = "GetPaginatedRetailerList", summary = "Get Paginated Retailer List", description = "Get Retailer List")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    @ResponseBody
    public DealerCurrentNetworkListDto getRetailerDetailedList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields
            , @Parameter(description="Search term") @RequestParam(required = false) final String searchKey
            , @Parameter(description="Network Type") @RequestParam(required = false) final String networkType
            , @RequestParam(required = false, defaultValue = "false") final Boolean isNew
            , @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                               @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                               final HttpServletResponse response)
    {
        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        DealerCurrentNetworkListData listData = new DealerCurrentNetworkListData();
       SearchPageData<DealerCurrentNetworkData> respone = nuvocoNetworkFacade.getRetailerDetailedSummaryList(searchKey,isNew,networkType,searchPageData);
        listData.setDealerCurrentNetworkList(respone.getResults());

       if (respone.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(respone.getPagination().getTotalNumberOfResults()));
        }
        return dataMapper.map(listData, DealerCurrentNetworkListDto.class, fields);

    }
}
