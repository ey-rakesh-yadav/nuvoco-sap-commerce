package com.nuvoco.controllers;


import com.nuvoco.facades.NuvocoCartFacade;
import de.hybris.platform.commercefacades.order.SaveCartFacade;
import de.hybris.platform.commercefacades.order.data.CommerceSaveCartParameterData;
import de.hybris.platform.commerceservices.order.CommerceSaveCartException;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.nuvoco.controllers.DealerAddressController.DEFAULT_FIELD_SET;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/carts")
@Tag(name = "Nuvoco Save Cart")

public class NuvocoSaveCartController extends NuvocooccController{


    @Resource(name = "saveCartFacade")
    private SaveCartFacade saveCartFacade;

    @Resource
    private NuvocoCartFacade nuvocoCartFacade;


    @RequestMapping(value = "/{cartId}/save/{employeeCode}", method = RequestMethod.PATCH)
    @ResponseBody
    @Operation(operationId = "doSaveCart", summary = "Explicitly saves a cart.", description = "Explicitly saves a cart.")
    @ApiBaseSiteIdAndUserIdParam
    public boolean doSaveCart(
            @Parameter(description = "Cart identifier: cart code for logged in user, cart guid for anonymous user, 'current' for the last modified cart", required = true) @PathVariable final String cartId,
            @Parameter(description = "The name that should be applied to the saved cart.") @RequestParam(value = "saveCartName", required = false) final String saveCartName,
            @Parameter(description = "The description that should be applied to the saved cart.") @RequestParam(value = "saveCartDescription", required = false) final String saveCartDescription,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @PathVariable String employeeCode) throws CommerceSaveCartException
    {

        final CommerceSaveCartParameterData parameters = new CommerceSaveCartParameterData();
        parameters.setCartId(cartId);
        parameters.setName(saveCartName);
        parameters.setDescription(saveCartDescription);

        return nuvocoCartFacade.saveCart(parameters, employeeCode);
    }


}
