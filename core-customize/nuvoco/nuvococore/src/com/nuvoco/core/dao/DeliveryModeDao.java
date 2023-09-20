package com.nuvoco.core.dao;

import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;

import java.util.List;

public interface DeliveryModeDao {

    List<DeliveryModeModel> findDeliveryModesByCode(final String code);


}
