package com.nuvoco.controllers;

import com.nuvoco.facades.SSOLoginFacade;
import com.nuvoco.facades.data.SSOLoginData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/{baseSiteId}/singleSignOnAuth")
@ApiVersion("v2")
@Tag(name = "SingleSignOn Auth Controller")
public class SSOLoginController {

    @Resource
    SSOLoginFacade ssoLoginFacade;

    @RequestMapping(value="/verifyUser", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdParam
    public ResponseEntity<SSOLoginData> validateUser(@RequestParam final String uid) throws UnknownIdentifierException
    {
        return ResponseEntity.status(HttpStatus.OK).body(ssoLoginFacade.verifyUser(uid));
    }



}

