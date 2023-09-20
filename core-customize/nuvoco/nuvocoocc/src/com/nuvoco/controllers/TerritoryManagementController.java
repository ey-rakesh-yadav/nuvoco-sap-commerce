package com.nuvoco.controllers;

import com.nuvoco.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.nuvoco.facades.TerritoryManagementFacade;
import com.nuvoco.facades.prosdealer.data.DealerListData;
import com.nuvoco.occ.dto.dealer.DealerListWsDTO;
import com.nuvoco.security.NuvocoSecuredAccessConstants;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/territoryManagement")
@ApiVersion("v2")
@Tag(name = "Territory Management Controller")
public class TerritoryManagementController {

    protected static final String DEFAULT_PAGE_SIZE = "20";
    protected static final String DEFAULT_CURRENT_PAGE = "0";

    protected static final String HEADER_TOTAL_COUNT = "X-Total-Count";
    private static final String DEFAULT_FIELD_SET = FieldSetLevelHelper.DEFAULT_LEVEL;

    protected static final String BASIC_FIELD_SET = FieldSetLevelHelper.BASIC_LEVEL;

    @Resource(name = "dataMapper")
    private DataMapper dataMapper;

    @Autowired
    private TerritoryManagementFacade territoryManagementFacade;

    @Secured({ NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP,NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getDealerListForRetailerPagination", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DealerListWsDTO getDealerListForRetailerPagination(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields
            , @Parameter(description="Search term") @RequestParam(required = false) final String filter
            , @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
            , @Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize)
    {
        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        DealerListData dealerListData=new DealerListData();
        SearchPageData<CustomerData> retailerListForDealerPagination = territoryManagementFacade.getDealerListForRetailerPagination(searchPageData,filter);
        dealerListData.setDealers(retailerListForDealerPagination.getResults());
        return dataMapper.map(dealerListData,DealerListWsDTO.class, BASIC_FIELD_SET);
    }


    @Secured({ NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP,NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getRetailerListForDealerPagination", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DealerListWsDTO getRetailerListForDealerPagination( @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields
            , @Parameter(description="Search term") @RequestParam(required = false) final String searchKey
            ,@Parameter(description="Network Type") @RequestParam(required = false) final String networkType
            ,@RequestParam(required = false, defaultValue = "false") final Boolean isNew
            ,@Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
            ,@Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize)
    {
        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        DealerListData dealerListData=new DealerListData();
        SearchPageData<CustomerData> retailerListForDealerPagination = territoryManagementFacade.getRetailerListForDealerPagination(searchPageData, networkType, isNew, searchKey);
        dealerListData.setDealers(retailerListForDealerPagination.getResults());
        return dataMapper.map(dealerListData,DealerListWsDTO.class, BASIC_FIELD_SET);
    }



    @Secured({ NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP,NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getRetailerCountForDealer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public Integer getRetailerCountForDealer()
    {
        return territoryManagementFacade.getRetailerCountForDealer();
    }



    @Secured({ NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP,NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getDealerCountForRetailer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public Integer getDealerCountForRetailer()
    {
        return territoryManagementFacade.getDealerCountForRetailer();
    }


    @Secured({ NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP,NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getRetailerListForDealer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DealerListWsDTO getRetailerListForDealer()
    {

        DealerListData dataList = territoryManagementFacade.getRetailerListForDealer();
        return dataMapper.map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
    }

    @Secured({ NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP,NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/dealersForSubArea", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public DealerListWsDTO getAllDealersForSubArea()
    {
        DealerListData dataList =  territoryManagementFacade.getAllDealersForSubArea();
        return dataMapper.map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
    }

    //New Territory Change
    @Secured({ NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP,NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/retailersForSubArea", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public DealerListWsDTO getAllRetailersForSubArea()
    {
        DealerListData dataList =  territoryManagementFacade.getAllRetailersForSubArea();
        return dataMapper.map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
    }


    @Secured({ NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP,NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/subAreasForCustomer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public List<String> getAllSubAreaForCustomer(@RequestParam final String customerId)
    {
        return territoryManagementFacade.getAllSubAreaForCustomer(customerId);
    }


    @Secured({ NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP,NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/retailersForSubAreaTOP", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public DealerListWsDTO getAllRetailersForSubAreaTOP(@RequestParam(required = false) final String subArea,@RequestParam final String dealerCode)
    {
        DealerListData dataList = territoryManagementFacade.getAllRetailersForSubAreaTOP(subArea,dealerCode);
        return dataMapper.map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);

    }


    @Secured({NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP,NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getAllStatesForSO", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public List<String> getAllStatesForSO()
    {
        return territoryManagementFacade.getAllStatesForSO();
    }
}
