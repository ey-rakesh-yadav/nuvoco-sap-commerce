package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.DealerDriverDetailsDao;
import com.nuvoco.core.model.DealerDriverDetailsModel;
import com.nuvoco.core.model.NuvocoCustomerModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.Collections;
import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DealerDriverDetailsDaoImpl extends DefaultGenericDao<DealerDriverDetailsModel> implements DealerDriverDetailsDao {

    public DealerDriverDetailsDaoImpl() {
        super(DealerDriverDetailsModel._TYPECODE);
    }


    /**
     * @param contactNumber
     * @return
     */
    @Override
    public DealerDriverDetailsModel findDriverDetailsByContactNumber(String contactNumber) {
        validateParameterNotNullStandardMessage("contactNumber", contactNumber);
        final List<DealerDriverDetailsModel> dealerDriverDetailsModels = this.find(Collections.singletonMap(DealerDriverDetailsModel.CONTACTNUMBER, contactNumber));
        if (dealerDriverDetailsModels.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d Driver Details with the contact number value: '%s', which should be unique", dealerDriverDetailsModels.size(),
                            contactNumber));
        }
        else
        {
            return dealerDriverDetailsModels.isEmpty() ? null : dealerDriverDetailsModels.get(0);
        }
    }

    /**
     * @param dealer
     * @return
     */
    @Override
    public List<DealerDriverDetailsModel> findDriverDetailsForDealer(NuvocoCustomerModel dealer) {
        validateParameterNotNull(dealer, "dealer  must not be null");
        return this.find(Collections.singletonMap(DealerDriverDetailsModel.DEALER, dealer));
    }
}
