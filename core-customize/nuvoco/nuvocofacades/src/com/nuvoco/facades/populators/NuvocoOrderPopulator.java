package com.nuvoco.facades.populators;

import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class NuvocoOrderPopulator implements Populator<OrderModel, OrderData> {


    /**
     * Populate the target instance with values from the source instance.
     *
     * @param source the source object
     * @param target  the target to fill
     * @throws ConversionException if an error occurs
     */
    @Override
    public void populate(OrderModel source, OrderData target) throws ConversionException {
        validateParameterNotNullStandardMessage("source", source);
        target.setOrderSource(null != source.getOrderSource() ? source.getOrderSource().getCode():null);
        //target.setEstimatedDeliveryDate(source.getEstimatedDeliveryDate());
        // target.setSelectedDeliveryDate(source.getSelectedDeliveryDate());
        //   target.setSelectedDeliverySlot(source.getSelectedDeliverySlot());
        target.setCustomerCategory(null != source.getCustomerCategory() ? source.getCustomerCategory().getCode():null);
    }
}
