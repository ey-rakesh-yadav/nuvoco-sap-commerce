package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.DistrictMasterDao;
import com.nuvoco.core.model.DistrictMasterModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistrictMasterDaoImpl extends DefaultGenericDao<DistrictMasterModel> implements DistrictMasterDao {


    public DistrictMasterDaoImpl() {
        super(DistrictMasterModel._TYPECODE);
    }

    /**
     * @param districtCode
     * @return
     */
    @Override
    public DistrictMasterModel findByCode(String districtCode) {
        if(districtCode!=null){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(DistrictMasterModel.CODE, districtCode);

            final List<DistrictMasterModel> districtMasterList = this.find(map);

            if(districtMasterList!=null && !districtMasterList.isEmpty()){
                return districtMasterList.get(0);
            }
        }
        return null;
    }
}
