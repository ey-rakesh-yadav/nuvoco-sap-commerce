package com.nuvoco.core.dao;

import de.hybris.platform.ordersplitting.model.WarehouseModel;

public interface NuvocoWarehouseDao {

    WarehouseModel findWarehouseByCode(final String warehouseCode);
}
