package com.nuvoco.facades;

import com.nuvoco.facades.data.NuvocoCustomerData;
import com.nuvoco.facades.data.NuvocoDealerSalesAllocationData;
import com.nuvoco.facades.data.vehicle.DealerDriverDetailsListData;
import com.nuvoco.facades.data.vehicle.DealerVehicleDetailsListData;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;

import java.util.List;

public interface DealerFacade {

    NuvocoCustomerData getCustomerProfile(String uid);
    NuvocoDealerSalesAllocationData getStockAllocationForRetailer(String productCode);

    NuvocoDealerSalesAllocationData getStockAllocationForDealer(String productCode);


    DealerVehicleDetailsListData getDealerVehicleDetails(final String dealerUid);

    DealerDriverDetailsListData getDealerDriverDetails(final String dealerUid);

    ErrorListWsDTO createDealerVehicleDetails(final DealerVehicleDetailsListData dealerVehicleDetailsListData, final String dealerUid);

    ErrorListWsDTO createDealerDriverDetails(final DealerDriverDetailsListData dealerDriverDetailsListData, final String dealerUid);

    ErrorWsDTO removeVehicle(final String vehicleNumber);

    ErrorWsDTO removeDriver(final String contactNumber);
}
