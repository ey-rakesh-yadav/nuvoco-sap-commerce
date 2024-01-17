package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.DJPRunDao;
import com.nuvoco.core.model.DJPRunMasterModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DJPRunDaoImpl extends DefaultGenericDao<DJPRunMasterModel> implements DJPRunDao {

    @Autowired
    UserService userService;

    public DJPRunDaoImpl() {
        super(DJPRunMasterModel._TYPECODE);
    }

    /**
     * @param plannedDate
     * @param district
     * @param taluka
     * @param brand
     * @return
     */
    @Override
    public DJPRunMasterModel findByPlannedDateAndUser(String plannedDate, String district, String taluka, String brand) {
        if(plannedDate!=null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(DJPRunMasterModel.PLANDATE, plannedDate);
            map.put(DJPRunMasterModel.DISTRICT, district);
            map.put(DJPRunMasterModel.TALUKA, taluka);
            map.put(DJPRunMasterModel.BRAND, brand);

            final List<DJPRunMasterModel> djpRunList = this.find(map);
            if(djpRunList!=null && !djpRunList.isEmpty())
                return djpRunList.get(0);
        }
        return null;
    }
}
