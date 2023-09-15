package com.nuvoco.core.services.impl;

import com.nuvoco.core.dao.NuvocoTruckDao;
import com.nuvoco.core.model.TruckModelMasterModel;
import com.nuvoco.core.services.DJPVisitService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DJPVisitServiceImpl implements DJPVisitService {

    @Autowired
    NuvocoTruckDao nuvocoTruckDao;


    /**
     * @return
     */
    @Override
    public List<TruckModelMasterModel> findAllTrucks() {
        List<TruckModelMasterModel> truckModelList = nuvocoTruckDao.findAllTrucks();
        return Objects.nonNull(truckModelList) ? truckModelList : Collections.emptyList();
    }
}
