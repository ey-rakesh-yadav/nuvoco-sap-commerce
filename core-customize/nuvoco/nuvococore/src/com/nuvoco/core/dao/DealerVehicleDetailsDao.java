package com.nuvoco.core.dao;

import com.nuvoco.core.model.DealerVehicleDetailsModel;
import com.nuvoco.core.model.NuvocoCustomerModel;

import java.util.List;

public interface DealerVehicleDetailsDao {

    DealerVehicleDetailsModel findVehicleDetailsByVehicleNumber(final String vehicleNumber);

    List<DealerVehicleDetailsModel> findVehicleDetailsForDealer(final NuvocoCustomerModel dealer);
}
