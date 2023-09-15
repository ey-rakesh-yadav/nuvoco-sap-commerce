package com.nuvoco.controllers;


import com.nuvoco.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.facades.DJPVisitFacade;
import com.nuvoco.facades.data.TruckModelData;
import com.nuvoco.security.NuvocoSecuredAccessConstants;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/visit")
@ApiVersion("v2")
@Tag(name = "DJP Visit Form Controller")
public class DJPVisitController {

    @Resource
    private DJPVisitFacade djpVisitFacade;

    @Secured({NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT })
    @RequestMapping(value="/truckList", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getAllTrucks", summary = " Truck List", description = "Get list of Truck data")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<List<TruckModelData>> getAllTrucks()
    {
        return ResponseEntity.status(HttpStatus.OK).body(djpVisitFacade.getAllTrucks());
    }
}
