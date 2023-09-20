package com.nuvoco.controllers;

import com.nuvoco.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.nuvoco.facades.CreditLimitData;
import com.nuvoco.facades.DealerFacade;
import com.nuvoco.facades.data.NuvocoCustomerData;
import com.nuvoco.facades.data.NuvocoDealerSalesAllocationData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = "/{baseSiteId}/dealer")
@ApiVersion("v2")
@Tag(name = "Nuvoco Dealer Controller")
public class NuvocoDealerController {

    @Autowired
    DealerFacade dealerFacade;
    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getCustomerProfile", method = RequestMethod.GET)
    @Operation(operationId = "GetCustomerProfile", summary = "Get Customer Profile")
    @ResponseBody
    @ApiBaseSiteIdParam
    public NuvocoCustomerData getCustomerProfile(@Parameter(description = "uid") @RequestParam String uid) throws Exception {
        return dealerFacade.getCustomerProfile(uid);
    }


    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/getDealerStockAllocation", method = RequestMethod.GET)
    @Operation(operationId = "getDealerStockAllocation", summary = "Get stock allocation for dealer and for a specified product")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NuvocoDealerSalesAllocationData getStockAllocationForDealer(@Parameter(description = "productCode", required=false) @RequestParam String productCode) {
        String dealerCode = null;
        return dealerFacade.getStockAllocationForDealer(productCode);
    }


    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/getRetailerStockAllocation", method = RequestMethod.GET)
    @Operation(operationId = "getRetailerStockAllocation", summary = "Get stock allocation of retailer for a specified product")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NuvocoDealerSalesAllocationData getStockAllocationForRetailer(@Parameter(description = "productCode", required=false) @RequestParam String productCode) {
        return dealerFacade.getStockAllocationForRetailer(productCode);
    }

}
