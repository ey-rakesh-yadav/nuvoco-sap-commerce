package com.nuvoco.core.dao;

import com.nuvoco.core.model.DistrictMasterModel;

public interface DistrictMasterDao {

    DistrictMasterModel findByCode(String districtCode);
}
