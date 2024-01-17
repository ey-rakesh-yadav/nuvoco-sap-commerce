package com.nuvoco.core.services;

import com.nuvoco.core.model.DealerDriverDetailsModel;
import com.nuvoco.core.model.DealerVehicleDetailsModel;
import com.nuvoco.core.model.NuvocoCustomerModel;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;

import java.util.List;

public interface DealerTransitService {

    boolean isDriverExisting(final String contactNumber);

    boolean isVehicleExisting(final String vehicleNumber);

    List<DealerVehicleDetailsModel> fetchVehicleDetailsForDealer(final NuvocoCustomerModel dealer);

    List<DealerDriverDetailsModel> fetchDriverDetailsForDealer(final NuvocoCustomerModel dealer);

    void saveVehicleDetailsForDealer(final List<DealerVehicleDetailsModel> dealerVehicleDetailsModelList , final String dealerUid);

    void saveDriverDetailsForDealer(final List<DealerDriverDetailsModel> dealerDriverDetailsModelList , final String dealerUid);

    ErrorWsDTO removeVehicle(final String vehicleNumber);

    ErrorWsDTO removeDriver(final String contactNumber);

}
