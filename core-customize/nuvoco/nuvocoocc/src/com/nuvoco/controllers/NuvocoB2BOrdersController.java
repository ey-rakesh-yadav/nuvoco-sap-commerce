package com.nuvoco.controllers;


import com.nuvoco.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.nuvoco.facades.NuvocoB2BOrderfacade;
import com.nuvoco.facades.data.*;
import com.nuvoco.facades.impl.NuvocoB2BCheckoutFacadeImpl;
import com.nuvoco.occ.dto.NuvocoOrderHistoryListWsDTO;
import com.nuvoco.occ.dto.order.NuvocoOrderWSDTO;
import com.nuvoco.security.NuvocoSecuredAccessConstants;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.b2bacceleratorfacades.api.cart.CartFacade;
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CartModificationDataList;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commerceservices.request.mapping.annotation.RequestMappingOverride;
import de.hybris.platform.commercewebservices.core.order.data.OrderEntryDataList;
import de.hybris.platform.commercewebservicescommons.annotation.SiteChannelRestriction;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderEntryListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.PaymentAuthorizationException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.commercewebservicescommons.strategies.CartLoaderStrategy;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import java.util.List;

import static com.nuvoco.constants.NuvocooccConstants.OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH;
import static de.hybris.platform.util.localization.Localization.getLocalizedString;

@Controller
@RequestMapping(value = OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH)
@ApiVersion("v2")
@Tag(name = "Nuvoco B2B Orders")
public class NuvocoB2BOrdersController extends NuvocooccController {


    private static final Logger LOGGER = Logger.getLogger(NuvocoB2BOrdersController.class);
    protected static final String HEADER_TOTAL_COUNT = "X-Total-Count";
    protected static final String DEFAULT_FIELD_SET = FieldSetLevelHelper.DEFAULT_LEVEL;
    protected static final String DEFAULT_PAGE_SIZE = "20";
    protected static final String DEFAULT_CURRENT_PAGE = "0";

    protected static final String API_COMPATIBILITY_B2B_CHANNELS = "api.compatibility.b2b.channels";

    private static final String CART_CHECKOUT_TERM_UNCHECKED = "cart.term.unchecked";

    @Autowired
    private DataMapper dataMapper;

    @Autowired
    private NuvocoB2BOrderfacade nuvocoB2BOrderfacade;

    @Autowired
    private UserFacade userFacade;

    @Resource(name = "cartLoaderStrategy")
    private CartLoaderStrategy cartLoaderStrategy;

    @Resource(name = "b2bCartFacade")
    private CartFacade cartFacade;

    @Resource(name = "b2BPlaceOrderCartValidator")
    private Validator placeOrderCartValidator;

    @Resource(name = "nuvocoB2BCheckoutFacade")
    private NuvocoB2BCheckoutFacadeImpl b2bCheckoutFacade;



    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getOrderHistoryForOrder", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getOrderHistoryForOrder", summary = "Get a scl  order history as per status", description = "Returns order listing.")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NuvocoOrderHistoryListWsDTO getOrderHistoryForOrder(@RequestParam(required = false) String orderStatus, @RequestParam(required = false) String filter, @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                               @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                               @Parameter(description = "Product Name field") @RequestParam(required = false) final String productName,
                                                               @Parameter(description = "Order Type  field") @RequestParam(required = false) final String orderType, @RequestParam(required = false, defaultValue = "all") final String spApprovalFilter,
                                                               @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, final HttpServletResponse response, @RequestParam(required = false, defaultValue = "false") final Boolean isCreditLimitBreached, @RequestParam(required = false, defaultValue = "false") final Boolean approvalPending)
    {
        NuvocoOrderHistoryListData sclOrderHistoryListData = new NuvocoOrderHistoryListData();

        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

        SearchPageData<NuvocoOrderHistoryData> orderHistoryForOrder = nuvocoB2BOrderfacade.getOrderHistoryForOrder(searchPageData, orderStatus, filter,productName,orderType,isCreditLimitBreached,spApprovalFilter,approvalPending);
        sclOrderHistoryListData.setOrdersList(orderHistoryForOrder.getResults());

        if (orderHistoryForOrder.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(orderHistoryForOrder.getPagination().getTotalNumberOfResults()));
        }
        return dataMapper.map(sclOrderHistoryListData, NuvocoOrderHistoryListWsDTO.class, fields);

    }


    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getOrderHistoryForOrderEntry", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getOrderHistoryForOrderEntry", summary = "Get a scl  order entry history as per status", description = "Returns order entry listing.")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NuvocoOrderHistoryListWsDTO getOrderHistoryForOrderEntry(@RequestParam(required = false) String orderStatus,@RequestParam(required = false) String filter, @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                                 @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                                 @Parameter(description = "Product Name field") @RequestParam(required = false) final String productName,
                                                                 @Parameter(description = "Order Type  field") @RequestParam(required = false) final String orderType,@RequestParam(required = false, defaultValue = "all") final String spApprovalFilter,
                                                                 @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,final HttpServletResponse response)
    {
        NuvocoOrderHistoryListData orderHistoryListData = new NuvocoOrderHistoryListData();

        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

        SearchPageData<NuvocoOrderHistoryData> orderHistoryForOrderEntry = nuvocoB2BOrderfacade.getOrderHistoryForOrderEntry(searchPageData, orderStatus, filter,productName,orderType,spApprovalFilter);
        orderHistoryListData.setOrdersList(orderHistoryForOrderEntry.getResults());

        if (orderHistoryForOrderEntry.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(orderHistoryForOrderEntry.getPagination().getTotalNumberOfResults()));
        }

        return dataMapper.map(orderHistoryListData, NuvocoOrderHistoryListWsDTO.class, fields);
    }



    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/optimalDeliveryWindow", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getOptimalDeliveryWindow", summary = "Get Optimal Delivery dates And Slots.", description = "Get Optimal Delivery dates And Slots.")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<DeliveryDateAndSlotListData> getOptimalDeliveryWindow(@RequestParam final String routeId
            , @PathVariable final String userId, @RequestParam final Double orderQty, @RequestParam String sourceCode, @RequestParam(defaultValue = "false") String isDealerProvidingTruck)
    {
        DeliveryDateAndSlotListData deliveryDateAndSlots = nuvocoB2BOrderfacade.fetchOptimalDeliveryWindow(orderQty, routeId, userId, sourceCode, isDealerProvidingTruck);
        return ResponseEntity.status(HttpStatus.OK).body(deliveryDateAndSlots);
    }

    @Secured(
            { NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP, NuvocoSecuredAccessConstants.ROLE_GUEST,
                    NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT })
    @RequestMapping(value = "/orders", method = RequestMethod.POST)
    @RequestMappingOverride(priorityProperty = "nuvocoocc.NuvocoB2BOrdersController.placeOrder.priority")
    @SiteChannelRestriction(allowedSiteChannelsProperty = API_COMPATIBILITY_B2B_CHANNELS)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    @Operation(operationId = "placeOrgOrder", summary = "Places a B2B Order.", description = "Places a B2B Order. By default the payment type is ACCOUNT. Please set payment type to CARD if placing an order using credit card.")
    public OrderWsDTO placeOrgOrder(
            @Parameter(description = "Cart identifier: cart code for logged in user, cart guid for anonymous user, 'current' for the last modified cart", required = true) @RequestParam(required = true) final String cartId,
            @Parameter(description = "Whether terms were accepted or not.", required = true) @RequestParam(required = true) final boolean termsChecked, @RequestParam(required = false) final String ncrGapAcceptanceReason,
            @ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields)
            throws InvalidCartException, PaymentAuthorizationException
    {
        try {
            validateTerms(termsChecked);

            validateUser();

            cartLoaderStrategy.loadCart(cartId);
            final CartData cartData = cartFacade.getCurrentCart();

            validateCart(cartData);
            validateAndAuthorizePayment(cartData);
            boolean isNTOrder = false;
            PlaceOrderData placeOrderData = new PlaceOrderData();
            placeOrderData.setIsNTOrder(isNTOrder);
            placeOrderData.setNcrGapAcceptanceReason(ncrGapAcceptanceReason);

            return dataMapper.map(b2bCheckoutFacade.placeOrder(placeOrderData), OrderWsDTO.class, fields);
        }
        catch(NullPointerException n) {
            OrderWsDTO nn = new OrderWsDTO();
            nn.setCode(ExceptionUtils.getStackTrace(n));
            return nn;
        }
    }

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/orders", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getSCLUserOrders", summary = "Get a scl  order.", description = "Returns specific order/Entry details based on a specific order code. The response contains detailed order information.")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<NuvocoOrderWSDTO> getUserOrders(
            @Parameter(description = "Order GUID (Globally Unique Identifier) or order CODE", required = true) @RequestParam final String code,
            @Parameter(description = "Order entry number") @RequestParam(required = false) final String entryNumber,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) throws Exception {
        try {
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug(String.format("Getting details for Order Code [%s] and Entry Number [%s]",code,entryNumber));
            }
            final NuvocoOrderData orderData = nuvocoB2BOrderfacade.getOrderDetails(code,entryNumber);
            return ResponseEntity.status(HttpStatus.OK).body(dataMapper.map(orderData, NuvocoOrderWSDTO.class, fields));

        }
        catch(Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }



    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/orders/{code}", method = RequestMethod.GET)
    @ResponseBody
    @RequestMappingOverride
    @Operation(operationId = "getOrderForCode", summary = "Get a scl  order detail.", description = "Returns specific order/Entry details based on a specific order code. The response contains detailed order information.")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public CartWsDTO getOrderForCode(
            @PathVariable final String code,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) throws  Exception {
        try {
            CartData cartData = nuvocoB2BOrderfacade.getOrderForCode(code);
            return dataMapper.map(cartData, CartWsDTO.class, fields);
        }
        catch(Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/orders/{code}/entries", method = RequestMethod.GET)
    @ResponseBody
    @RequestMappingOverride
    @Operation(operationId = "getOrderEntryForCode", summary = "Get a scl  order entry detail.", description = "Returns specific order/Entry details based on a specific order code. The response contains detailed order information.")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public OrderEntryListWsDTO getOrderEntryForCode(
            @PathVariable final String code,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        final OrderEntryDataList dataList = new OrderEntryDataList();
        CartData cartData = nuvocoB2BOrderfacade.getOrderForCode(code);
        dataList.setOrderEntries(cartData.getEntries());
        return dataMapper.map(dataList, OrderEntryListWsDTO.class, fields);
    }

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getCancelOrderHistoryForOrder", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getCancelOrderHistoryForOrder", summary = "Get a scl cancel order history as per status", description = "Returns order listing.")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NuvocoOrderHistoryListWsDTO getCancelOrderHistoryForOrder(@RequestParam(required = false) String orderStatus, @RequestParam(required = false) String filter, @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                                  @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                                  @Parameter(description = "Product Name field") @RequestParam(required = false) final String productName,
                                                                  @Parameter(description = "Order Type  field") @RequestParam(required = false) final String orderType,@RequestParam(required = false, defaultValue = "all") final String spApprovalFilter,
                                                                  @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,final HttpServletResponse response, @Parameter(description = "Filters by Month") @RequestParam(required = false, defaultValue = "0") Integer month,
                                                                  @Parameter(description = "Filters by Year") @RequestParam(required = false, defaultValue = "0") Integer year)
    {
        NuvocoOrderHistoryListData sclOrderHistoryListData = new NuvocoOrderHistoryListData();

        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

        SearchPageData<NuvocoOrderHistoryData> orderHistoryForOrder = nuvocoB2BOrderfacade.getCancelOrderHistoryForOrder(searchPageData, orderStatus, filter,productName,orderType,spApprovalFilter,month,year);
        sclOrderHistoryListData.setOrdersList(orderHistoryForOrder.getResults());

        if (orderHistoryForOrder.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(orderHistoryForOrder.getPagination().getTotalNumberOfResults()));
        }
        return dataMapper.map(sclOrderHistoryListData, NuvocoOrderHistoryListWsDTO.class, fields);

    }


    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getCancelOrderHistoryForOrderEntry", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getCancelOrderHistoryForOrderEntry", summary = "Get a scl cancel order entry history as per status", description = "Returns order entry listing.")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NuvocoOrderHistoryListWsDTO getCancelOrderHistoryForOrderEntry(@RequestParam(required = false) String orderStatus,@RequestParam(required = false) String filter, @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                                       @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                                       @Parameter(description = "Product Name field") @RequestParam(required = false) final String productName,
                                                                       @Parameter(description = "Order Type  field") @RequestParam(required = false) final String orderType,@RequestParam(required = false, defaultValue = "all") final String spApprovalFilter,
                                                                       @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,final HttpServletResponse response, @Parameter(description = "Filters by Month") @RequestParam(required = false, defaultValue = "0") Integer month,
                                                                       @Parameter(description = "Filters by Year") @RequestParam(required = false, defaultValue = "0") Integer year)
    {
        NuvocoOrderHistoryListData orderHistoryListData = new NuvocoOrderHistoryListData();

        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

        SearchPageData<NuvocoOrderHistoryData> orderHistoryForOrderEntry = nuvocoB2BOrderfacade.getCancelOrderHistoryForOrderEntry(searchPageData, orderStatus, filter,productName,orderType,spApprovalFilter,month,year);
        orderHistoryListData.setOrdersList(orderHistoryForOrderEntry.getResults());

        if (orderHistoryForOrderEntry.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(orderHistoryForOrderEntry.getPagination().getTotalNumberOfResults()));
        }

        return dataMapper.map(orderHistoryListData, NuvocoOrderHistoryListWsDTO.class, fields);
    }




    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getVehicleArrivalConfirmationForOrder", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getVehicleArrivalConfirmationForOrder", summary = "Get vehicle arrival confirmation as per status", description = "Vehicle Arrival Confirmation.")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public Boolean getVehicleArrivalConfirmationForOrder(@RequestParam boolean vehicleArrived, @RequestParam String orderCode, @RequestParam String entryNumber)
    {
        return nuvocoB2BOrderfacade.getVehicleArrivalConfirmationForOrder(vehicleArrived, orderCode, entryNumber);
    }


    @Secured(
            { NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,
                    NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP, "ROLE_CLIENT" })
    @RequestMapping(value = "/cartFromOrder", method = RequestMethod.POST)
    @SiteChannelRestriction(allowedSiteChannelsProperty = API_COMPATIBILITY_B2B_CHANNELS)
    @RequestMappingOverride
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createCartFromOrder", summary = "Create a cart based on a previous order", description = "Returns a list of modification applied to the new cart compared to original. e.g lower quantity was added")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public String createCartFromOrder(
            @Parameter(description = "The order code", required = true) @RequestParam final String orderCode,
            @Parameter(description = "Response configuration. This is the list of fields that should be returned in the response body.", schema = @Schema(allowableValues = {"BASIC", "DEFAULT", "FULL"})) @RequestParam(defaultValue = "DEFAULT") final String fields,
            @Parameter(hidden = true) final HttpServletResponse response)
    {
        b2bCheckoutFacade.createCartFromOrder(orderCode);
        CartData cartData = commerceCartFacade.getSessionCart();
        return cartData.getCode();
    }



    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getOrderFromRetailersRequest", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getOrderFromRetailersRequest", summary = "\n" +
            "Orders from retailers- self stock or reject request", description = "\n" +
            "Orders from retailers- self stock or reject request.")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public Boolean getOrderFromRetailersRequest(@RequestParam String requisitionId, @RequestParam String status) {
        return nuvocoB2BOrderfacade.getOrderFromRetailersRequest(requisitionId, status);
    }

    @Secured(
            { NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,
                    NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP, "ROLE_CLIENT" })
    @RequestMapping(value = "/cartFromOrderEntry", method = RequestMethod.POST)
    @SiteChannelRestriction(allowedSiteChannelsProperty = API_COMPATIBILITY_B2B_CHANNELS)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createCartFromOrder", summary = "Create a cart based on a previous order Entry", description = "Returns a list of modification applied to the new cart compared to original. e.g lower quantity was added")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public String createCartFromOrder(
            @Parameter(description = "The order code", required = true) @RequestParam final String orderCode,
            @Parameter(description = "The order Entry Number", required = true) @RequestParam final String orderEntryNumber,
            @Parameter(description = "Response configuration. This is the list of fields that should be returned in the response body.", schema = @Schema( allowableValues ={ "BASIC", "DEFAULT", "FULL"})) @RequestParam(defaultValue = "DEFAULT") final String fields,
            @Parameter(hidden = true) final HttpServletResponse response)
    {
        if(StringUtils.isNotBlank(orderEntryNumber)) {
            b2bCheckoutFacade.createCartFromOrder(orderCode);

            CartData cartData = commerceCartFacade.getSessionCart();
            int inputEntryEnumber = Integer.valueOf(orderEntryNumber);
            for(int i=cartData.getEntries().size()-1;i>=0;i--) {
                int entryNumber = cartData.getEntries().get(i).getEntryNumber();
                if(inputEntryEnumber != entryNumber) {
                    try {
                        commerceCartFacade.updateCartEntry(entryNumber, 0);
                    } catch (CommerceCartModificationException e) {
                        cartFacade.removeSessionCart();
                        throw new IllegalArgumentException("Unable to create cart from the given order. Cart cannot be modified", e);
                    }
                }
            }
            nuvocoB2BOrderfacade.updateTotalQuantity(cartData.getEntries().get(0).getQuantity());
            return cartData.getCode();
        }
        else {
            throw new IllegalArgumentException("Unable to create cart from the given order. Entry Number cannot be empty");
        }
    }

    protected void validateAndAuthorizePayment(final CartData cartData)
            throws PaymentAuthorizationException
    {
        if (CheckoutPaymentType.CARD.getCode().equals(cartData.getPaymentType().getCode()) && !b2bCheckoutFacade.authorizePayment(null))
        {
            throw new PaymentAuthorizationException();
        }
    }

    protected void validateCart(final CartData cartData) throws InvalidCartException
    {
        final Errors errors = new BeanPropertyBindingResult(cartData, "sessionCart");
        placeOrderCartValidator.validate(cartData, errors);
        if (errors.hasErrors())
        {
            throw new WebserviceValidationException(errors);
        }

        try
        {
            final List<CartModificationData> modificationList = cartFacade.validateCurrentCartData();
            if(CollectionUtils.isNotEmpty(modificationList))
            {
                final CartModificationDataList cartModificationDataList = new CartModificationDataList();
                cartModificationDataList.setCartModificationList(modificationList);
                throw new WebserviceValidationException(cartModificationDataList);
            }
        }
        catch (final CommerceCartModificationException e)
        {
            throw new InvalidCartException(e);
        }
    }
    protected void validateTerms(final boolean termsChecked)
    {
        if (!termsChecked)
        {
            throw new RequestParameterException(getLocalizedString(CART_CHECKOUT_TERM_UNCHECKED));
        }
    }


    protected void validateUser()
    {
        if (userFacade.isAnonymousUser())
        {
            throw new AccessDeniedException("Access is denied");
        }
    }
    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getEpodFeedback", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getEpodFeedback", summary = "Get EPOD Feedback", description = "Returns Feedback.")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public Boolean getEpodFeedback(@Parameter(description = "Data object that contains information necessary for order feedback details", required = true) @RequestBody final EpodFeedbackData epodFeedbackData)
    {
        return nuvocoB2BOrderfacade.getEpodFeedback(epodFeedbackData);

    }


    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/updateEpodStatusForOrder", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "updateEpodStatusForOrder", summary = "Get EPOD status", description = "Get EPOD Status.")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public Boolean updateEpodStatusForOrder(@RequestParam double shortageQuantity, @RequestParam String orderCode, @RequestParam int entryNumber)
    {
        return nuvocoB2BOrderfacade.updateEpodStatusForOrder(shortageQuantity,orderCode, entryNumber);
    }


    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getEpodListBasedOnOrderStatus", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getEpodListBasedOnOrderStatus", summary = "Get EPOD List as per status", description = "Returns EPOD List.")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NuvocoOrderHistoryListWsDTO getEpodListBasedOnOrderStatus(@RequestParam List<String> Status, @RequestParam(required = false) String filter, @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                                  @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                                  @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, final HttpServletResponse response)
    {
        NuvocoOrderHistoryListData orderHistoryListData = new NuvocoOrderHistoryListData();

        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

        SearchPageData<NuvocoOrderHistoryData> orderHistoryForOrderEntry = nuvocoB2BOrderfacade.getEpodListBasedOnOrderStatus(searchPageData, Status, filter);
        orderHistoryListData.setOrdersList(orderHistoryForOrderEntry.getResults());

        if (orderHistoryForOrderEntry.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(orderHistoryForOrderEntry.getPagination().getTotalNumberOfResults()));
        }

        return dataMapper.map(orderHistoryListData, NuvocoOrderHistoryListWsDTO.class, fields);

    }
}
