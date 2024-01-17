package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.DeliverySlotMasterDao;
import com.nuvoco.core.model.DeliverySlotMasterModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.List;

public class DeliverySlotMasterDaoImpl extends DefaultGenericDao<DeliverySlotMasterModel>  implements DeliverySlotMasterDao {


    public DeliverySlotMasterDaoImpl() {
        super(DeliverySlotMasterModel._TYPECODE);
    }

    /**
     * @return
     */
    @Override
    public List<DeliverySlotMasterModel> findAll() {
        return this.find();
    }
}
