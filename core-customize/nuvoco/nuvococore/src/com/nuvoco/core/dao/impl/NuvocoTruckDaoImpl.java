package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.NuvocoTruckDao;
import com.nuvoco.core.model.TruckModelMasterModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.List;

public class NuvocoTruckDaoImpl extends DefaultGenericDao<TruckModelMasterModel> implements NuvocoTruckDao {
    public NuvocoTruckDaoImpl() {
        super(TruckModelMasterModel._TYPECODE);
    }

    /**
     * @return
     */
    @Override
    public List<TruckModelMasterModel> findAllTrucks() {
        return this.find();
    }
}
