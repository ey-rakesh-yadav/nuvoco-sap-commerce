package com.nuvoco.core.dao;

import com.nuvoco.core.model.TruckModelMasterModel;

import java.util.List;

public interface NuvocoTruckDao {

    public List<TruckModelMasterModel> findAllTrucks();
}
