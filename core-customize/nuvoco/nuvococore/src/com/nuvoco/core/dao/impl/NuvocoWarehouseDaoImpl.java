package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.NuvocoWarehouseDao;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.Collections;
import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class NuvocoWarehouseDaoImpl extends DefaultGenericDao<WarehouseModel> implements NuvocoWarehouseDao {
    public NuvocoWarehouseDaoImpl() {
        super(WarehouseModel._TYPECODE);
    }

    /**
     * @param warehouseCode
     * @return
     */
    @Override
    public WarehouseModel findWarehouseByCode(String warehouseCode) {
        validateParameterNotNullStandardMessage("warehouseCode", warehouseCode);
        final List<WarehouseModel> warehouseList = this.find(Collections.singletonMap(WarehouseModel.CODE, warehouseCode));
        if (warehouseList.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d warehouses with the warehouseCode value: '%s', which should be unique", warehouseList.size(),
                            warehouseCode));
        }
        else
        {
            return warehouseList.isEmpty() ? null : warehouseList.get(0);
        }
    }
}
