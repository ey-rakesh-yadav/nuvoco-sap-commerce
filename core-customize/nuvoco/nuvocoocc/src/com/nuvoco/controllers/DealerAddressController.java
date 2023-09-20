package com.nuvoco.controllers;


import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.services.NuvocoCustomerService;
import com.nuvoco.facades.NuvocoCustomerFacade;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commercewebservices.core.user.data.AddressDataList;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.StringUtils;
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
import javax.annotation.security.PermitAll;
import java.util.List;
import java.util.stream.Collectors;

import static de.hybris.platform.webservicescommons.util.YSanitizer.sanitize;


@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/dealerAddresses")
@ApiVersion("v2")
@CacheControl(directive = CacheControlDirective.PRIVATE)
@Tag(name = "Dealer Address")
@PermitAll
public class DealerAddressController {


    protected static final String DEFAULT_FIELD_SET = FieldSetLevelHelper.DEFAULT_LEVEL;
    private static final String OBJECT_NAME_ADDRESS_ID = "addressId";

    private static final String OBJECT_NAME_ADDRESS = "address";

    private static final String OBJECT_NAME_ADDRESS_DATA = "addressData";

    public static final String ADDRESS_DOES_NOT_EXIST = "Address with given id: '%s' doesn't exist or belong to another user";
    private static final Logger LOG = LoggerFactory.getLogger(DealerAddressController.class);
    private static final String ADDRESS_MAPPING = "line1,line2,city,district,taluka,state,retailerAddressPk,postalCode,latitude,longitude,erpCity,country,retailerUid,retailerName,defaultAddress,isPrimaryAddress,accountName,cellphone,email";
    @Autowired
    UserFacade userFacade;

    @Autowired
    UserService userService;

    @Autowired
    NuvocoCustomerFacade nuvocoCustomerFacade;
    @Autowired
    NuvocoCustomerService nuvocoCustomerService;
    @Autowired
    DataMapper dataMapper;

    @Resource(name = "addressDTOValidator")
    private Validator addressDTOValidator;

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getAddresses", summary = "Get customer's addresses", description = "Returns customer's addresses.")
    @ApiBaseSiteIdAndUserIdParam
    @ApiResponse(responseCode = "200",description = "List of customer's addresses")
    public AddressListWsDTO getAddresses(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                         @Parameter(description = "retailer uid") @RequestParam(required = false) final String retailerUid,
                                         @Parameter(description = "retailerAddressPk") @RequestParam(required = false) final String retailerAddressPk )
    {
        final List<AddressData> addressList = userFacade.getAddressBook();
        List<AddressData> filteredAddressList = addressList;
        if(StringUtils.isNotBlank(retailerUid)){
            filteredAddressList = nuvocoCustomerFacade.filterAddressBookData(addressList,retailerUid);
        }
        if(StringUtils.isNotBlank(retailerAddressPk)){
            filteredAddressList = filteredAddressList.stream().filter(address->address.getRetailerAddressPk()!=null && address.getRetailerAddressPk().equals(retailerAddressPk)).collect(Collectors.toList());
        }

        NuvocoCustomerModel currentUser = (NuvocoCustomerModel) userService.getCurrentUser();

        if(!(currentUser.getGroups().contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
            filteredAddressList = filteredAddressList.stream().filter(address->address.getErpId()!=null).collect(Collectors.toList());
        }
        filteredAddressList = nuvocoCustomerFacade.filterAddressByLpSource(filteredAddressList);


        final AddressDataList addressDataList = new AddressDataList();
        addressDataList.setAddresses(filteredAddressList);
        return dataMapper.map(addressDataList, AddressListWsDTO.class, fields);
    }



    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(operationId = "createAddress", summary = "Creates a new address.", description = "Creates a new address.")
    @ApiBaseSiteIdAndUserIdParam
    public AddressWsDTO createAddress(
            @Parameter(description = "Address object.", required = true) @RequestBody final AddressWsDTO address,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        //validate(address, OBJECT_NAME_ADDRESS, getAddressDTOValidator());

        final AddressData addressData = dataMapper.map(address, AddressData.class, ADDRESS_MAPPING);
        addressData.setShippingAddress(true);
        addressData.setVisibleInAddressBook(true);

        userFacade.addAddress(addressData);
//out bound action
        /*NuvocoCustomerModel currentUser = (NuvocoCustomerModel) userService.getCurrentUser();

        if((currentUser.getGroups().contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
            nuvocoCustomerService.triggerShipToPartyAddress(addressData.getId());
        }*/

        return dataMapper.map(addressData, AddressWsDTO.class, fields);
    }


    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/{addressId}", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getAddress", summary = "Get info about address", description = "Returns detailed information about address with a given id.")
    @ApiBaseSiteIdAndUserIdParam
    public AddressWsDTO getAddress(@Parameter(description = "Address identifier.", required = true) @PathVariable final String addressId,
                                   @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        LOG.debug("getAddress: id={}", sanitize(addressId));
        final AddressData addressData = getAddressData(addressId);
        return dataMapper.map(addressData, AddressWsDTO.class, fields);
    }


    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/{addressId}", method = RequestMethod.PATCH, consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    @Operation(operationId = "updateAddress", summary = "Updates the address", description = "Updates the address. Only attributes provided in the request body will be changed.")
    @ApiBaseSiteIdAndUserIdParam
    @ResponseStatus(HttpStatus.OK)
    public void updateAddress(@Parameter(description = "Address identifier.", required = true) @PathVariable final String addressId,
                              @Parameter(description = "Address object", required = true) @RequestBody final AddressWsDTO address)
    {
        final AddressData addressData = getAddressData(addressId);
        final boolean isAlreadyDefaultAddress = addressData.isDefaultAddress();
        addressData.setFormattedAddress(null);

        dataMapper.map(address, addressData, ADDRESS_MAPPING, false);
        validate(addressData, OBJECT_NAME_ADDRESS, addressDTOValidator);

        if (addressData.getId().equals(userFacade.getDefaultAddress().getId()))
        {
            addressData.setDefaultAddress(true);
            addressData.setVisibleInAddressBook(true);
        }
        if (!isAlreadyDefaultAddress && addressData.isDefaultAddress())
        {
            userFacade.setDefaultAddress(addressData);
        }
        userFacade.editAddress(addressData);
    }


    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/{addressId}", method = RequestMethod.DELETE)
    @Operation(operationId = "removeAddress", summary = "Delete customer's address.", description = "Removes customer's address.")
    @ApiBaseSiteIdAndUserIdParam
    @ResponseStatus(HttpStatus.OK)
    public void removeAddress(@Parameter(description = "Address identifier.", required = true) @PathVariable final String addressId)
    {
        LOG.debug("removeAddress: id={}", sanitize(addressId));
        final AddressData address = getAddressData(addressId);
        userFacade.removeAddress(address);
    }


    private AddressData getAddressData(final String addressId)
    {
        final AddressData addressData = userFacade.getAddressForCode(addressId);
        if (addressData == null)
        {
            throw new RequestParameterException(String.format(ADDRESS_DOES_NOT_EXIST, sanitize(addressId)),
                    RequestParameterException.INVALID, OBJECT_NAME_ADDRESS_ID);
        }
        return addressData;
    }

    protected void validate(final Object object, final String objectName, final Validator validator)
    {
        final Errors errors = new BeanPropertyBindingResult(object, objectName);
        validator.validate(object, errors);
        if (errors.hasErrors())
        {
            throw new WebserviceValidationException(errors);
        }
    }
}
