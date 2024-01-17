package com.nuvoco.facades.populators;

import com.nuvoco.core.model.DealerVehicleDetailsModel;
import com.nuvoco.facades.data.vehicle.DealerVehicleDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DealerVehicleDetailsReversePopulator implements Populator<DealerVehicleDetailsData, DealerVehicleDetailsModel> {


    @Override
    public void populate(DealerVehicleDetailsData source, DealerVehicleDetailsModel target) throws ConversionException {

        validateParameterNotNullStandardMessage("source", source);
        validateParameterNotNullStandardMessage("target", target);

        if(StringUtils.isNotBlank(source.getCapacity())){
            target.setCapacity(Double.valueOf(source.getCapacity()));
        }
        target.setVehicleNumber(source.getVehicleNumber());
        target.setMake(source.getMake());
        target.setModel(source.getModel());
    }
}
