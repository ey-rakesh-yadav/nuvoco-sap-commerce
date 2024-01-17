package com.nuvoco.facades.impl;

import com.nuvoco.core.model.DeliverySlotMasterModel;
import com.nuvoco.core.services.NuvocoB2BOrderService;
import com.nuvoco.facades.DeliverySlotsFacade;
import com.nuvoco.facades.data.DeliverySlotMasterData;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.ArrayList;
import java.util.List;

public class DeliverySlotsFacadeImpl implements DeliverySlotsFacade {


    @Autowired
    private NuvocoB2BOrderService nuvocoB2BOrderService;

    /**
     * @return
     */
    @Override
    public List<DeliverySlotMasterData> getDeliverySlotList() {
        List<DeliverySlotMasterData>  dataList = new ArrayList<DeliverySlotMasterData>();
        List<DeliverySlotMasterModel> modelList = nuvocoB2BOrderService.getDeliverySlotList();
        for(DeliverySlotMasterModel model: modelList) {
            DeliverySlotMasterData data = new DeliverySlotMasterData();
            data.setDisplayName(model.getDisplayName());
            data.setEnd(model.getEnd());
            data.setStart(model.getStart());
            data.setSequence(model.getSequence());
            data.setEnumName(model.getSlot().getCode());
            dataList.add(data);
        }
        return dataList;
    }
}
