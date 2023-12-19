package com.nuvoco.core.dao;

import com.nuvoco.core.model.RouteMasterModel;
import com.nuvoco.core.model.SubAreaMasterModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

import java.util.List;

public interface RouteMasterDao {

    RouteMasterModel findByRouteId(String routeIds);

    List<RouteMasterModel> findBySubAreaAndBrand(SubAreaMasterModel subAreaMaster, BaseSiteModel brand);

}
