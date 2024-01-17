package com.nuvoco.core.dao;

import com.nuvoco.core.model.RegionMasterModel;

public interface RegionMasterDao {

    RegionMasterModel findByCode(String regionCode);
}
