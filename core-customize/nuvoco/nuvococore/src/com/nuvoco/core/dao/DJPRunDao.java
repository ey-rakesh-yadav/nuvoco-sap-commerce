package com.nuvoco.core.dao;

import com.nuvoco.core.model.DJPRunMasterModel;

public interface DJPRunDao {

    DJPRunMasterModel findByPlannedDateAndUser(String plannedDate, String district, String taluka, String brand);

}
