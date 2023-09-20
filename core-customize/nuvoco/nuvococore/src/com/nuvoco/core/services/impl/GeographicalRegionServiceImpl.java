package com.nuvoco.core.services.impl;

import com.nuvoco.core.dao.GeographicalRegionDao;
import com.nuvoco.core.services.GeographicalRegionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class GeographicalRegionServiceImpl implements GeographicalRegionService {

    @Autowired
    GeographicalRegionDao geographicalRegionDao;
    /**
     * @param state
     * @param district
     * @param taluka
     * @param erpCity
     * @return
     */
    @Override
    public List<String> getBusinessState(String state, String district, String taluka, String erpCity) {
        return geographicalRegionDao.getBusinessState(state, district, taluka, erpCity);
    }

    /**
     * @param customerCode
     * @return
     */
    @Override
    public List<String> findUserState(String customerCode) {
        return geographicalRegionDao.findUserState(customerCode);
    }

    /**
     * @param state
     * @param district
     * @param taluka
     * @return
     */
    @Override
    public List<String> findAllLpSourceErpCity(String state, String district, String taluka) {
        return geographicalRegionDao.findAllLpSourceErpCity(state, district, taluka);
    }

    /**
     * @param state
     * @param district
     * @return
     */
    @Override
    public List<String> findAllLpSourceTaluka(String state, String district) {
        return geographicalRegionDao.findAllLpSourceTaluka(state, district);
    }

    /**
     * @param state
     * @return
     */
    @Override
    public List<String> findAllLpSourceDistrict(String state) {
        return geographicalRegionDao.findAllLpSourceDistrict(state);
    }
}
