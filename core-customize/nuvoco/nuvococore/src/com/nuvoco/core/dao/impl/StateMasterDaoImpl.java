package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.StateMasterDao;
import com.nuvoco.core.model.StateMasterModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateMasterDaoImpl extends DefaultGenericDao<StateMasterModel> implements StateMasterDao {

    public StateMasterDaoImpl()
    {
        super(StateMasterModel._TYPECODE);
    }
    /**
     * @param stateCode
     * @return
     */
    @Override
    public StateMasterModel findByCode(String stateCode) {
        if(stateCode!=null){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(StateMasterModel.CODE,stateCode);

            final List<StateMasterModel> stateMasterList = this.find(map);
            if(stateMasterList!=null && !stateMasterList.isEmpty()){
                return stateMasterList.get(0);
            }
        }
        return null;
    }
}
