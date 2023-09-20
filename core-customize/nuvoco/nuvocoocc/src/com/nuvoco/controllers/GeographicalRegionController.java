package com.nuvoco.controllers;

import com.nuvoco.facades.GeographicalRegionFacade;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping(value = "{baseSiteId}/geographicalRegion/")
@ApiVersion("v2")
@Tag(name = "State District Taluka ErpCity Management")
@PermitAll
public class GeographicalRegionController extends NuvocooccController{


    @Autowired
    GeographicalRegionFacade geographicalRegionFacade;

    @RequestMapping(value="/getBusinessState", method = RequestMethod.POST)
    @ResponseBody
    @ApiBaseSiteIdParam
    public List<String> getBusinessState(@Parameter(description = "geographyState") @RequestParam final String geographyState
            , @Parameter(description = "district") @RequestParam final String district, @Parameter(description = "taluka") @RequestParam final String taluka, @Parameter(description = "erpCity") @RequestParam final String erpCity)
    {
        return geographicalRegionFacade.getBusinessState(geographyState, district, taluka, erpCity);
    }


    @RequestMapping(value="/lpSourceErpCities", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getLpSourceErpCities", summary = "LPSource ErpCity List", description = "Get list of erpCity")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<String>> findAllLpSourceErpCity(@Parameter(description = "state") @RequestParam final String state, @Parameter(description = "district") @RequestParam final String district, @Parameter(description = "taluka") @RequestParam final String taluka)
    {
        List<String> erpCities = new ArrayList<String>(geographicalRegionFacade.findAllLpSourceErpCity(state, district, taluka));
        Collections.sort(erpCities);
        return ResponseEntity.status(HttpStatus.OK).body(erpCities);
    }

    @RequestMapping(value="/lpSourceTalukas", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getLpSourceTaluka", summary = "LPSource Taluka List", description = "Get list of Taluka")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<String>> findAllLpSourceTaluka(@Parameter(description = "state") @RequestParam final String state,@Parameter(description = "district") @RequestParam final String district)
    {
        List<String> erpCities = new ArrayList<String>(geographicalRegionFacade.findAllLpSourceTaluka(state, district));
        Collections.sort(erpCities);
        return ResponseEntity.status(HttpStatus.OK).body(erpCities);
    }

    @RequestMapping(value="/lpSourceDistricts", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getLpSourceDistrict", summary = "LPSource District List", description = "Get list of District")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<String>> findAllLpSourceDistrict(@Parameter(description = "state") @RequestParam final String state)
    {
        List<String> erpCities = new ArrayList<String>(geographicalRegionFacade.findAllLpSourceDistrict(state));
        Collections.sort(erpCities);
        return ResponseEntity.status(HttpStatus.OK).body(erpCities);
    }

    @RequestMapping(value="/userStates", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "userStates", summary = "User State List", description = "Get User State")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<String>> findUserState(@RequestParam(required = false) String customerCode)
    {
        List<String> states = new ArrayList<String>(geographicalRegionFacade.findUserState(customerCode));
        Collections.sort(states);
        return ResponseEntity.status(HttpStatus.OK).body(states);
    }
}
