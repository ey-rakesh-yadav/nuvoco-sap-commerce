package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.DataConstraintDao;
import com.nuvoco.core.model.DataConstraintModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataConstraintDaoImpl  extends DefaultGenericDao<DataConstraintModel> implements DataConstraintDao {



    public DataConstraintDaoImpl() {
        super(DataConstraintModel._TYPECODE);
    }
    /**
     * @param constraintName
     * @return
     */
    @Override
    public Integer findDaysByConstraintName(String constraintName) {
        if(constraintName!=null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(DataConstraintModel.CONSTRAINTNAME, constraintName);

            final List<DataConstraintModel> dataList = this.find(map);
            if(dataList!=null && !dataList.isEmpty())
                return dataList.get(0).getDay();
        }
        return null;
    }
}
