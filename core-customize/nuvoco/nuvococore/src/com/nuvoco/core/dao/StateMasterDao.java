package com.nuvoco.core.dao;

import com.nuvoco.core.model.StateMasterModel;

public interface StateMasterDao {

    StateMasterModel findByCode(String stateCode);
}
