package com.nuvoco.controllers;


import com.nuvoco.core.enums.OrderType;
import com.nuvoco.facades.NuvocoCartFacade;
import com.nuvoco.security.NuvocoSecuredAccessConstants;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.CartAddressException;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdUserIdAndCartIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static de.hybris.platform.webservicescommons.util.YSanitizer.sanitize;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/carts")
@ApiVersion("v2")
@Tag(name = "Nuvoco Carts")
public class NuvocoCartsController extends NuvocooccController{


    private static final Logger LOG = LoggerFactory.getLogger(NuvocoCartsController.class);
    @Autowired
    CartFacade cartFacade;


    @Autowired
    UserService userService;

    @Autowired
    NuvocoCartFacade nuvocoCartFacade;

    @Autowired
    SessionService sessionService;

    @Resource(name = "cartDetailsValidator")
    private Validator cartDetailsValidator;

    @Resource(name = "checkoutFacade")
    private CheckoutFacade checkoutFacade;

    @Resource(name = "deliveryAddressValidator")
    private Validator deliveryAddressValidator;

    @Autowired
    private DataMapper dataMapper;

    private static final String ACTING_USER_UID = "ACTING_USER_UID";



    @Secured({NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT, NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP, NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(method = RequestMethod.POST,  value="/{cartId}/confirmOrderDetails", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @Operation(operationId = "confirmOrderDetails", summary = "Confirm Order Details", description = "Confirm Order Details and save it to the cart")
    @ApiBaseSiteIdUserIdAndCartIdParam
    public CartWsDTO confirmOrderDetails(
            @Parameter(description = "Data object that contains information necessary for confirm order details", required = true) @RequestBody final CartWsDTO cartDetails,
            @ApiFieldsParam @RequestParam(defaultValue = FULL_FIELD_SET) final String fields) throws CommerceCartModificationException {
        try {
            LOG.debug("confirm order details");
            validate(cartDetails, "cartDetails", cartDetailsValidator);
            setCartDeliveryAddressInternal(cartDetails);
            CartData cartData = setCartDetailsInternal(cartDetails);
            nuvocoCartFacade.setOrderRequistionOnOrder(cartDetails);
            return dataMapper.map(cartData, CartWsDTO.class, fields);
        }
        catch(NullPointerException n) {
            CartWsDTO nn = new CartWsDTO();
            nn.setName(ExceptionUtils.getStackTrace(n));
            return nn;
        }
    }

    private CartData setCartDetailsInternal(CartWsDTO cartDetails) {

        sessionService.getAttribute(ACTING_USER_UID);
        UserModel currentUser = userService.getCurrentUser();

        if (nuvocoCartFacade.setCartDetails(cartDetails)) {
            return cartFacade.getSessionCart();
        }
        return null; //todo
    }

    private void setCartDeliveryAddressInternal(CartWsDTO cartDetails)
    {
        if(!OrderType.ISO.getCode().equals(cartDetails.getOrderType())) {
            String addressId = null;
            if (cartDetails.getDeliveryAddress()==null)
            {
                throw new CartAddressException("Address given by id " + sanitize(addressId) + " is not valid",
                        CartAddressException.NOT_VALID, addressId);
            }
            addressId = cartDetails.getDeliveryAddress().getId();
            LOG.info(String.format("setCartDeliveryAddressInternal: for address id %s",addressId));

            final AddressData address = new AddressData();
            address.setId(addressId);

            final Errors errors = new BeanPropertyBindingResult(address, "addressData");
            deliveryAddressValidator.validate(address, errors);
            if (errors.hasErrors())
            {
                throw new CartAddressException("Address given by id " + sanitize(addressId) + " is not valid",
                        CartAddressException.NOT_VALID, addressId);
            }

            if (!checkoutFacade.setDeliveryAddress(address))
            {
                throw new CartAddressException(
                        "Address given by id " + sanitize(addressId) + " cannot be set as delivery address in this cart",
                        CartAddressException.CANNOT_SET, addressId);
            }
        }
    }
}
