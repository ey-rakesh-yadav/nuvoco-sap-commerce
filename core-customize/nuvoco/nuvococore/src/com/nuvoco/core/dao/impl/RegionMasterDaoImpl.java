package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.RegionMasterDao;
import com.nuvoco.core.model.RegionMasterModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionMasterDaoImpl extends DefaultGenericDao<RegionMasterModel> implements RegionMasterDao {


    public RegionMasterDaoImpl() {
        super(RegionMasterModel._TYPECODE);
    }


    /**
     * @param regionCode
     * @return
     */
    @Override
    public RegionMasterModel findByCode(String regionCode) {
        if(regionCode!=null){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(RegionMasterModel.CODE,regionCode);

            final List<RegionMasterModel> regionMasterList = this.find(map);
            if(regionMasterList!=null && !regionMasterList.isEmpty()){
                return regionMasterList.get(0);
            }
        }
        return null;
    }
}
