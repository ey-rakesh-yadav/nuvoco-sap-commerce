package com.nuvoco.controllers;


import com.nuvoco.facades.DealerFacade;
import com.nuvoco.facades.data.vehicle.DealerDriverDetailsData;
import com.nuvoco.facades.data.vehicle.DealerDriverDetailsListData;
import com.nuvoco.facades.data.vehicle.DealerVehicleDetailsData;
import com.nuvoco.facades.data.vehicle.DealerVehicleDetailsListData;
import com.nuvoco.occ.dto.vehicle.DealerDriverDetailsListWsDTO;
import com.nuvoco.occ.dto.vehicle.DealerDriverDetailsWsDTO;
import com.nuvoco.occ.dto.vehicle.DealerVehicleDetailsListWsDTO;
import com.nuvoco.occ.dto.vehicle.DealerVehicleDetailsWsDTO;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "{baseSiteId}/users/{userId}/dealerfleet")
@ApiVersion("v2")
@Tag(name = "Dealer Fleet Management")
@PermitAll
public class NuvocoDealerFleetController {
    private static final Logger LOG = Logger.getLogger(NuvocoDealerFleetController.class);


    protected static final String DEFAULT_FIELD_SET = FieldSetLevelHelper.DEFAULT_LEVEL;
    @Resource
    DealerFacade dealerFacade;

    @Resource(name = "dataMapper")
    private DataMapper dataMapper;

    @RequestMapping(value = "/vehicles", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getVehicleDetailsForDealer", summary = "Gets List of Vehicles for the Dealer", description = "Gets List of Vehicles for the Dealer")
    @ApiBaseSiteIdAndUserIdParam
    public DealerVehicleDetailsListWsDTO getVehicleDetailsForDealer(
            @Parameter(description = "User ID identifier") @PathVariable final String userId) {
        final DealerVehicleDetailsListData dealerVehicleDetailsData = dealerFacade.getDealerVehicleDetails(userId);
        return dataMapper.map(dealerVehicleDetailsData, DealerVehicleDetailsListWsDTO.class,DEFAULT_FIELD_SET);
    }


    @RequestMapping(value = "/vehicles", method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createAndSaveVehicleDetailsForDealer", summary = "Creates and persists List of Vehicles for the Dealer", description = "Creates and persists List of Vehicles for the Dealer")
    @ApiBaseSiteIdAndUserIdParam
    public ErrorListWsDTO createAndSaveVehicleDetailsForDealer(
            @Parameter(description = "User ID identifier") @PathVariable final String userId,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
            @Parameter(description = "Request body parameter that contains attributes for creating the Vehicle details", required = true)
            @RequestBody final DealerVehicleDetailsListWsDTO dealerVehicleDetailsListWsDTO) {
        DealerVehicleDetailsListData dealerVehicleDetailsListData = getDealerVehicleDetails(dealerVehicleDetailsListWsDTO);
        if(CollectionUtils.isNotEmpty(dealerVehicleDetailsListData.getVehicleDetails())){
            return dealerFacade.createDealerVehicleDetails(dealerVehicleDetailsListData,userId);
        }
        else{
            return getEmptyRequestErrorMessage();
        }

    }



    @RequestMapping(value = "/drivers", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getDriverDetailsForDealer", summary = "Gets List of Drivers for the Dealer", description = "Gets List of Drivers for the Dealer")
    @ApiBaseSiteIdAndUserIdParam
    public DealerDriverDetailsListWsDTO DriverDetailsForDealer(
            @Parameter(description = "User ID identifier") @PathVariable final String userId) {
        final DealerDriverDetailsListData dealerDriverDetailsData = dealerFacade.getDealerDriverDetails(userId);
        return dataMapper.map(dealerDriverDetailsData, DealerDriverDetailsListWsDTO.class,DEFAULT_FIELD_SET);
    }

    @RequestMapping(value = "/drivers", method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createAndSaveDriverDetailsForDealer", summary = "Creates and persists List of Drivers for the Dealer", description = "Creates and persists List of Drivers for the Dealer")
    @ApiBaseSiteIdAndUserIdParam
    public ErrorListWsDTO createAndSaveDriverDetailsForDealer(
            @Parameter(description = "User ID identifier") @PathVariable final String userId,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
            @Parameter(description = "Request body parameter that contains attributes for creating the Driver details", required = true)
            @RequestBody final DealerDriverDetailsListWsDTO dealerDriverDetailsListWsDTO) {

        DealerDriverDetailsListData dealerDriverDetailsListData = getDealerDriverDetails(dealerDriverDetailsListWsDTO);
        if(CollectionUtils.isNotEmpty(dealerDriverDetailsListData.getDriverDetails())){
            return dealerFacade.createDealerDriverDetails(dealerDriverDetailsListData,userId);
        }
        else{
            return getEmptyRequestErrorMessage();
        }
    }


    @RequestMapping(value = "/vehicle/{vehicleNumber}", method = RequestMethod.DELETE)
    @Operation(operationId = "removeVehicle", summary = "Delete dealer's vehicle.", description = "Removes dealer's vehicle.")
    @ApiBaseSiteIdAndUserIdParam
    @ResponseStatus(HttpStatus.OK)
    public ErrorWsDTO removeDealerVehicle(@Parameter(description = "vehicle number identifier.", required = true) @PathVariable final String vehicleNumber) {

        LOG.debug("removeVehicle: Vehicle Number:"+ vehicleNumber);
        return dealerFacade.removeVehicle(vehicleNumber);
    }

    @RequestMapping(value = "/driver/{contactNumber}", method = RequestMethod.DELETE)
    @Operation(operationId = "removeDriver", summary = "Delete dealer's Driver.", description = "Removes dealer's driver.")
    @ApiBaseSiteIdAndUserIdParam
    @ResponseStatus(HttpStatus.OK)
    public ErrorWsDTO removeDealerDriver(@Parameter(description = "contact number identifier.", required = true) @PathVariable final String contactNumber) {

        LOG.debug("removeDriver: Contact Number:"+ contactNumber);
        return dealerFacade.removeDriver(contactNumber);
    }

    private DealerVehicleDetailsListData getDealerVehicleDetails(DealerVehicleDetailsListWsDTO dealerVehicleDetailsListWsDTO) {

        DealerVehicleDetailsListData dealerVehicleDetailsListData = new DealerVehicleDetailsListData();
        List<DealerVehicleDetailsData> dealerVehicleDetailsDataList = new ArrayList<>();
        if(null!= dealerVehicleDetailsListWsDTO && CollectionUtils.isNotEmpty(dealerVehicleDetailsListWsDTO.getVehicleDetails())){
            for(DealerVehicleDetailsWsDTO dealerVehicleDetailsWsDTO :dealerVehicleDetailsListWsDTO.getVehicleDetails()){
                DealerVehicleDetailsData dealerVehicleDetailsData = new DealerVehicleDetailsData();
                dealerVehicleDetailsData.setVehicleNumber(dealerVehicleDetailsWsDTO.getVehicleNumber());
                dealerVehicleDetailsData.setModel(dealerVehicleDetailsWsDTO.getModel());
                dealerVehicleDetailsData.setCapacity(dealerVehicleDetailsWsDTO.getCapacity());
                dealerVehicleDetailsData.setMake(dealerVehicleDetailsWsDTO.getMake());
                dealerVehicleDetailsDataList.add(dealerVehicleDetailsData);
            }
        }
        dealerVehicleDetailsListData.setVehicleDetails(dealerVehicleDetailsDataList);
        return dealerVehicleDetailsListData;
    }


    private DealerDriverDetailsListData getDealerDriverDetails(DealerDriverDetailsListWsDTO dealerDriverDetailsListWsDTO) {

        DealerDriverDetailsListData dealerDriverDetailsListData = new DealerDriverDetailsListData();
        List<DealerDriverDetailsData> dealerDriverDetailsDataList = new ArrayList<>();
        if(null!= dealerDriverDetailsListWsDTO && CollectionUtils.isNotEmpty(dealerDriverDetailsListWsDTO.getDriverDetails())){
            for(DealerDriverDetailsWsDTO dealerDriverDetailsWsDTO :dealerDriverDetailsListWsDTO.getDriverDetails()){
                DealerDriverDetailsData dealerDriverDetailsData = new DealerDriverDetailsData();
                dealerDriverDetailsData.setDriverName(dealerDriverDetailsWsDTO.getDriverName());
                dealerDriverDetailsData.setContactNumber(dealerDriverDetailsWsDTO.getContactNumber());
                dealerDriverDetailsDataList.add(dealerDriverDetailsData);
            }
        }
        dealerDriverDetailsListData.setDriverDetails(dealerDriverDetailsDataList);
        return dealerDriverDetailsListData;
    }

    protected ErrorListWsDTO getEmptyRequestErrorMessage(){
        ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        List<ErrorWsDTO> errors = new ArrayList<>();
        ErrorWsDTO errorWsDTO = new ErrorWsDTO();
        errorWsDTO.setReason("Request is Invalid");
        errors.add(errorWsDTO);
        errorListWsDTO.setErrors(errors);
        return errorListWsDTO;
    }

}
