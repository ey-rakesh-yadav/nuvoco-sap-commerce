package com.nuvoco.controllers;

import com.nuvoco.core.enums.OrderType;
import com.nuvoco.facades.TradeOrderFacade;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.security.PermitAll;
import java.util.List;

@Controller
@RequestMapping(value = "{baseSiteId}/tradeOrder/")
@ApiVersion("v2")
@Tag(name = "Trade Order Management")
@PermitAll
public class TradeOrderController extends NuvocooccController{

    @Autowired
    TradeOrderFacade tradeOrderFacade;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getAllTradeOrderType", summary = " Trade Order Type List", description = "Get list of Trade Order Types")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<OrderType>> getAllTradeOrderTypes()
    {
        return ResponseEntity.status(HttpStatus.OK).body(tradeOrderFacade.listAllTradeOrderType());
    }
}
