package com.nuvoco.facades.populators;

import com.nuvoco.core.model.DealerDriverDetailsModel;
import com.nuvoco.facades.data.vehicle.DealerDriverDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DealerDriverDetailsPopulator implements Populator<DealerDriverDetailsModel, DealerDriverDetailsData> {

    @Override
    public void populate(DealerDriverDetailsModel source, DealerDriverDetailsData target) throws ConversionException {

        validateParameterNotNullStandardMessage("source", source);
        validateParameterNotNullStandardMessage("target", target);

        target.setDriverName(source.getDriverName());
        target.setContactNumber(source.getContactNumber());
    }
}
