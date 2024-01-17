package com.nuvoco.core.services.impl;

import com.nuvoco.core.dao.DealerDriverDetailsDao;
import com.nuvoco.core.dao.DealerVehicleDetailsDao;
import com.nuvoco.core.model.DealerDriverDetailsModel;
import com.nuvoco.core.model.DealerVehicleDetailsModel;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.services.DealerTransitService;
import com.nuvoco.core.services.NuvocoCustomerService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;

public class DealerTransitServiceImpl implements DealerTransitService {


    @Autowired
    DealerVehicleDetailsDao dealerVehicleDetailsDao;

    @Autowired
    private DealerDriverDetailsDao dealerDriverDetailsDao;

    @Autowired
    private NuvocoCustomerService nuvocoCustomerService;
    @Autowired
    private ModelService modelService;

    private static final String VEHICLE_NOT_PRESENT_ERROR_MESSAGE = "Vehicle Not Present with Vehicle Number : %s";
    private static final String DRIVER_NOT_PRESENT_ERROR_MESSAGE = "Driver Not Present with Contact Number : %s";

    /**
     * @param contactNumber
     * @return
     */
    @Override
    public boolean isDriverExisting(String contactNumber) {
        DealerDriverDetailsModel dealerDriverDetailsModel = dealerDriverDetailsDao.findDriverDetailsByContactNumber(contactNumber);
        if(null != dealerDriverDetailsModel){
            return Boolean.TRUE;
        }
        else{
            return Boolean.FALSE;
        }
    }

    /**
     * @param vehicleNumber
     * @return
     */
    @Override
    public boolean isVehicleExisting(String vehicleNumber) {

        DealerVehicleDetailsModel dealerVehicleDetailsModel = dealerVehicleDetailsDao.findVehicleDetailsByVehicleNumber(vehicleNumber);
        if(null!= dealerVehicleDetailsModel){
            return Boolean.TRUE;
        }
        else{
            return Boolean.FALSE;
        }

    }

    /**
     * @param dealer
     * @return
     */
    @Override
    public List<DealerVehicleDetailsModel> fetchVehicleDetailsForDealer(NuvocoCustomerModel dealer) {
        return dealerVehicleDetailsDao.findVehicleDetailsForDealer(dealer);
    }

    /**
     * @param dealer
     * @return
     */
    @Override
    public List<DealerDriverDetailsModel> fetchDriverDetailsForDealer(NuvocoCustomerModel dealer) {
        return dealerDriverDetailsDao.findDriverDetailsForDealer(dealer);
    }

    /**
     * @param dealerVehicleDetailsModelList
     * @param dealerUid
     */
    @Override
    public void saveVehicleDetailsForDealer(List<DealerVehicleDetailsModel> dealerVehicleDetailsModelList, String dealerUid) {
        if(CollectionUtils.isNotEmpty(dealerVehicleDetailsModelList)){
            dealerVehicleDetailsModelList.forEach(dealerVehicleDetailsMode -> dealerVehicleDetailsMode.setDealer(nuvocoCustomerService.getCustomerForUid(dealerUid)));
            modelService.saveAll(dealerVehicleDetailsModelList);
        }
    }

    /**
     * @param dealerDriverDetailsModelList
     * @param dealerUid
     */
    @Override
    public void saveDriverDetailsForDealer(List<DealerDriverDetailsModel> dealerDriverDetailsModelList, String dealerUid) {
        if(CollectionUtils.isNotEmpty(dealerDriverDetailsModelList)){
            dealerDriverDetailsModelList.forEach(dealerDriverDetailsModel -> dealerDriverDetailsModel.setDealer(nuvocoCustomerService.getCustomerForUid(dealerUid)));
            modelService.saveAll(dealerDriverDetailsModelList);
        }
    }

    /**
     * @param vehicleNumber
     * @return
     */
    @Override
    public ErrorWsDTO removeVehicle(String vehicleNumber) {
        DealerVehicleDetailsModel dealerVehicleDetailsModel = dealerVehicleDetailsDao.findVehicleDetailsByVehicleNumber(vehicleNumber);
        if(null == dealerVehicleDetailsModel){
            ErrorWsDTO errorWsDTO = new ErrorWsDTO();
            errorWsDTO.setErrorCode(vehicleNumber);
            errorWsDTO.setReason(String.format(VEHICLE_NOT_PRESENT_ERROR_MESSAGE,vehicleNumber));
            return errorWsDTO;
        }
        else{
            modelService.remove(dealerVehicleDetailsModel);
            return new ErrorWsDTO();
        }
    }

    /**
     * @param contactNumber
     * @return
     */
    @Override
    public ErrorWsDTO removeDriver(String contactNumber) {
        DealerDriverDetailsModel dealerDriverDetailsModel = dealerDriverDetailsDao.findDriverDetailsByContactNumber(contactNumber);
        if(null == dealerDriverDetailsModel){
            ErrorWsDTO errorWsDTO = new ErrorWsDTO();
            errorWsDTO.setErrorCode(contactNumber);
            errorWsDTO.setReason(String.format(DRIVER_NOT_PRESENT_ERROR_MESSAGE,contactNumber));
            return errorWsDTO;
        }
        else{
            modelService.remove(dealerDriverDetailsModel);
            return new ErrorWsDTO();
        }
    }
}
