package com.nuvoco.core.dao;

import java.util.List;

public interface GeographicalRegionDao {

    List<String> getBusinessState(java.lang.String state, java.lang.String district, java.lang.String taluka, java.lang.String erpCity);

    List<String> findAllLpSourceDistrict(String state);

    List<String> findAllLpSourceTaluka(String state, String district);

    List<String> findAllLpSourceErpCity(String state, String district, String taluka);

    List<String> findUserState(String customerCode);

}
