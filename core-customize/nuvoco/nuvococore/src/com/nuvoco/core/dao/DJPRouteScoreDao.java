package com.nuvoco.core.dao;

import com.nuvoco.core.model.DJPRouteScoreMasterModel;

public interface DJPRouteScoreDao {

    DJPRouteScoreMasterModel findByRouteScoreId(String routeScoreId);
}
