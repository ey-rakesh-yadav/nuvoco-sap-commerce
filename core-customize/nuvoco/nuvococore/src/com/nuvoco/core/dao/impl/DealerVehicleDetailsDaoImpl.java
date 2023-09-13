package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.DealerVehicleDetailsDao;
import com.nuvoco.core.model.DealerVehicleDetailsModel;
import com.nuvoco.core.model.NuvocoCustomerModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.Collections;
import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DealerVehicleDetailsDaoImpl extends DefaultGenericDao<DealerVehicleDetailsModel> implements DealerVehicleDetailsDao {


    public DealerVehicleDetailsDaoImpl() {
        super(DealerVehicleDetailsModel._TYPECODE);
    }



    /**
     * @param vehicleNumber
     * @return
     */
    @Override
    public DealerVehicleDetailsModel findVehicleDetailsByVehicleNumber(String vehicleNumber) {
        validateParameterNotNullStandardMessage("vehicleNumber", vehicleNumber);
        final List<DealerVehicleDetailsModel> dealerVehicleDetailsModels = this.find(Collections.singletonMap(DealerVehicleDetailsModel.VEHICLENUMBER, vehicleNumber));
        if (dealerVehicleDetailsModels.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d Vehicle Details with the contact number value: '%s', which should be unique", dealerVehicleDetailsModels.size(),
                            vehicleNumber));
        }
        else
        {
            return dealerVehicleDetailsModels.isEmpty() ? null : dealerVehicleDetailsModels.get(0);
        }
    }

    /**
     * @param dealer
     * @return
     */
    @Override
    public List<DealerVehicleDetailsModel> findVehicleDetailsForDealer(NuvocoCustomerModel dealer) {
        validateParameterNotNull(dealer, "dealer  must not be null");
        return this.find(Collections.singletonMap(DealerVehicleDetailsModel.DEALER, dealer));
    }


}
