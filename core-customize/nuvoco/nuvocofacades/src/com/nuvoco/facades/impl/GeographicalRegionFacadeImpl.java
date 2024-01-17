package com.nuvoco.facades.impl;

import com.nuvoco.core.services.GeographicalRegionService;
import com.nuvoco.facades.GeographicalRegionFacade;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class GeographicalRegionFacadeImpl implements GeographicalRegionFacade {

    @Autowired
    GeographicalRegionService geographicalRegionService;

    /**
     * @param state
     * @param district
     * @param taluka
     * @param erpCity
     * @return
     */
    @Override
    public List<String> getBusinessState(String state, String district, String taluka, String erpCity) {
       return geographicalRegionService.getBusinessState(state,district,taluka,erpCity);
    }

    /**
     * @param customerCode
     * @return
     */
    @Override
    public List<String> findUserState(String customerCode) {
        return geographicalRegionService.findUserState(customerCode);

    }

    /**
     * @param state
     * @param district
     * @param taluka
     * @return
     */
    @Override
    public List<String> findAllLpSourceErpCity(String state, String district, String taluka) {
        return geographicalRegionService.findAllLpSourceErpCity(state, district, taluka);

    }

    /**
     * @param state
     * @param district
     * @return
     */
    @Override
    public List<String> findAllLpSourceTaluka(String state, String district) {
        return geographicalRegionService.findAllLpSourceTaluka(state, district);
    }

    /**
     * @param state
     * @return
     */
    @Override
    public List<String> findAllLpSourceDistrict(String state) {
        return geographicalRegionService.findAllLpSourceDistrict(state);
    }
}


