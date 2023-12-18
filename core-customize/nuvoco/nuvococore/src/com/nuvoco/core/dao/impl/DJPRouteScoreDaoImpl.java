package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.DJPRouteScoreDao;
import com.nuvoco.core.model.DJPRouteScoreMasterModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.Collections;
import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DJPRouteScoreDaoImpl extends DefaultGenericDao<DJPRouteScoreMasterModel> implements DJPRouteScoreDao {

    public DJPRouteScoreDaoImpl() {
        super(DJPRouteScoreMasterModel._TYPECODE);
    }


    /**
     * @param routeScoreId
     * @return
     */
    @Override
    public DJPRouteScoreMasterModel findByRouteScoreId(String routeScoreId) {
        validateParameterNotNullStandardMessage("routeScoreId", routeScoreId);
        final List<DJPRouteScoreMasterModel> routeScoreList = this.find(Collections.singletonMap(DJPRouteScoreMasterModel.ID, routeScoreId));
        if (routeScoreList.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d routeScores with the routeScoreId value: '%s', which should be unique", routeScoreList.size(),
                            routeScoreId));
        }
        else
        {
            return routeScoreList.isEmpty() ? null : routeScoreList.get(0);
        }
    }
}
