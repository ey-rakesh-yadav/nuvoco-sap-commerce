package com.nuvoco.facades.populators;

import com.nuvoco.core.model.DealerDriverDetailsModel;
import com.nuvoco.facades.data.vehicle.DealerDriverDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DealerDriverDetailsReversePopulator implements Populator<DealerDriverDetailsData, DealerDriverDetailsModel> {

    @Override
    public void populate(DealerDriverDetailsData source, DealerDriverDetailsModel target) throws ConversionException {


        validateParameterNotNullStandardMessage("source", source);
        validateParameterNotNullStandardMessage("target", target);

        target.setDriverName(source.getDriverName());
        target.setContactNumber(source.getContactNumber());
    }
}
