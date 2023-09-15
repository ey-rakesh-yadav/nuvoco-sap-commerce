package com.nuvoco.controllers;

import com.nuvoco.facades.CreditLimitData;
import com.nuvoco.facades.DealerFacade;
import com.nuvoco.facades.data.NuvocoCustomerData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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


}
