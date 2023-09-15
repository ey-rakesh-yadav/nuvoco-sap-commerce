package com.nuvoco.facades.impl;

import com.nuvoco.core.model.TruckModelMasterModel;
import com.nuvoco.core.services.DJPVisitService;
import com.nuvoco.facades.DJPVisitFacade;
import com.nuvoco.facades.data.TruckModelData;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class DJPVisitFacadeImpl implements DJPVisitFacade {


    @Autowired
    DJPVisitService djpVisitService;

    /**
     * @return
     */
    @Override
    public List<TruckModelData> getAllTrucks() {
        List<TruckModelMasterModel> modelList =  djpVisitService.findAllTrucks();
        List<TruckModelData> dataList = new ArrayList<TruckModelData>();
        modelList.stream().forEach(truck->
        {
            TruckModelData data = new TruckModelData();
            data.setTruckModel(truck.getTruckModel());
            data.setCapacity(truck.getCapacity());
            data.setVehicleMake(truck.getVehicleMake());
            data.setVehicleType(String.valueOf(truck.getVehicleType()));
            //data.setCount(truck.getCount());
            dataList.add(data);
        });
        return dataList;
    }
}
