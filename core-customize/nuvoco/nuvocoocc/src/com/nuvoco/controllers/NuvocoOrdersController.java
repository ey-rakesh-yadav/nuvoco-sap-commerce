package com.nuvoco.controllers;


import com.nuvoco.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.nuvoco.facades.NuvocoB2BOrderfacade;
import com.nuvoco.facades.NuvocoCartFacade;
import com.nuvoco.facades.NuvocoOrderFacade;
import com.nuvoco.facades.data.DestinationSourceListData;
import com.nuvoco.facades.data.EpodFeedbackData;
import com.nuvoco.occ.dto.DestinationSourceListWsDTO;
import com.nuvoco.security.NuvocoSecuredAccessConstants;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;

import static com.nuvoco.controllers.DealerAddressController.DEFAULT_FIELD_SET;

@Controller
@RequestMapping(value = "{baseSiteId}/users/{userId}/nuvocoOrder/")
@ApiVersion("v2")
@Tag(name = "Nuvoco Order Management")
@PermitAll
public class NuvocoOrdersController {


    @Autowired
    NuvocoOrderFacade nuvocoOrderFacade;

    @Autowired
    NuvocoCartFacade nuvocoCartFacade;

    @Autowired
    NuvocoB2BOrderfacade nuvocoB2BOrderfacade;

    @Autowired
    DataMapper dataMapper;

    @RequestMapping(value = "/orderCountByStatus", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getOrderCountByStatus", summary = "Order Count by Status", description = "Get count of Order as per the Order status")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<Integer> getOrdersCountByStatus(@RequestParam String orderStatus, @RequestParam(required = false, defaultValue = "false") Boolean approvalPending) {
        return ResponseEntity.status(HttpStatus.OK).body(nuvocoOrderFacade.getOrderCountByStatus(orderStatus, approvalPending));
    }

    @RequestMapping(value = "/orderEntryCountByStatus", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getOrderEntryCountByStatus", summary = "Order Entry Count by Status", description = "Get count of Order Entry as per the Order status")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<Integer> getOrderEntriesCountByStatus(@RequestParam String orderStatus) {
        return ResponseEntity.status(HttpStatus.OK).body(nuvocoOrderFacade.getOrderEntryCountByStatus(orderStatus));
    }


    @RequestMapping(value = "/cancelOrderCountByStatus", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getCancelOrderCountByStatus", summary = "Cancel Order Count by Status", description = "Get count of Order as per the Order status")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<Integer> getCancelOrdersCountByStatus(@RequestParam String orderStatus) {
        return ResponseEntity.status(HttpStatus.OK).body(nuvocoOrderFacade.getCancelOrderCountByStatus(orderStatus));
    }

    @RequestMapping(value = "/cancelOrderEntryCountByStatus", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getCancelOrderEntryCountByStatus", summary = "Cancel Order Entry Count by Status", description = "Get count of Order Entry as per the Order status")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<Integer> getCancelOrderEntriesCountByStatus(@RequestParam String orderStatus) {
        return ResponseEntity.status(HttpStatus.OK).body(nuvocoOrderFacade.getCancelOrderEntryCountByStatus(orderStatus));
    }

    @Secured({ NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT, NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/fetchDestinationSource",method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(operationId = "fetchDestinationSource", summary = "select source as per order type", description = "select source as per order type")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public DestinationSourceListWsDTO fetchDestinationSource(
            @RequestParam (required = false) final String city,
            @RequestParam final String orderType, @RequestParam(required = false) final String deliveryMode, @RequestParam(required = false) final String productCode, @RequestParam(required = false) final String district,
            @RequestParam(required = false) final String state,
            @RequestParam(required = false) final String taluka,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {

        final DestinationSourceListData destinationSourceListData = nuvocoCartFacade.fetchDestinationSource(city,orderType,deliveryMode,productCode,district,state,taluka);
        return dataMapper.map(destinationSourceListData, DestinationSourceListWsDTO.class, fields);
    }


}
