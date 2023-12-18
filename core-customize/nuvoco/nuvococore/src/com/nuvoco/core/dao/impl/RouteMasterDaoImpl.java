package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.RouteMasterDao;
import com.nuvoco.core.model.RouteMasterModel;
import com.nuvoco.core.model.SubAreaMasterModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class RouteMasterDaoImpl extends DefaultGenericDao<RouteMasterModel> implements RouteMasterDao {


    public RouteMasterDaoImpl() {
        super(RouteMasterModel._TYPECODE);
    }

    /**
     * @param routeId
     * @return
     */
    @Override
    public RouteMasterModel findByRouteId(String routeId) {
        validateParameterNotNullStandardMessage("routeId", routeId);
        final List<RouteMasterModel> routeModelList = this.find(Collections.singletonMap(RouteMasterModel.ROUTEID, routeId));
        if (routeModelList.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d routes with the routeId value: '%s', which should be unique", routeModelList.size(),
                            routeId));
        }
        else
        {
            return routeModelList.isEmpty() ? null : routeModelList.get(0);
        }
    }

    /**
     * @param subAreaMaster
     * @param brand
     * @return
     */
    @Override
    public List<RouteMasterModel> findBySubAreaAndBrand(SubAreaMasterModel subAreaMaster, BaseSiteModel brand) {
        validateParameterNotNullStandardMessage("subAreaMaster", subAreaMaster);
        validateParameterNotNullStandardMessage("brand", brand);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(RouteMasterModel.SUBAREAMASTER, subAreaMaster);
        map.put(RouteMasterModel.BRAND, brand.getUid());
        map.put(RouteMasterModel.ACTIVE, Boolean.TRUE);

        final List<RouteMasterModel> routeMasterList = this.find(map);
        return routeMasterList;
    }
}
