package com.nuvoco.facades;

import java.util.List;

public interface GeographicalRegionFacade {

    List<String> getBusinessState(String state, String district, String taluka, String erpCity);

    List<String> findUserState(String customerCode);

    List<String> findAllLpSourceErpCity(String state, String district, String taluka);
    List<String> findAllLpSourceTaluka(String state, String district);
    List<String> findAllLpSourceDistrict(String state);

}
