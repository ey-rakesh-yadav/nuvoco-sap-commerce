package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.NuvocoSalesOrderDeliverySLADao;
import com.nuvoco.core.model.SalesOrderDeliverySLAModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

public class NuvocoSalesOrderDeliverySLADaoImpl implements NuvocoSalesOrderDeliverySLADao {


    private static final String FIND_SALES_ORDER_DELIVERY_SLA_BY_ROUTE = "SELECT {"+SalesOrderDeliverySLAModel.PK+"} FROM {"+SalesOrderDeliverySLAModel._TYPECODE+
            "} WHERE {"+SalesOrderDeliverySLAModel.ROUTE+"} = ?route ";

    @Autowired
    FlexibleSearchService flexibleSearchService;

    /**
     * @param routeId
     * @return
     */
    @Override
    public SalesOrderDeliverySLAModel findByRoute(String routeId) {
        validateParameterNotNull(routeId, "routeId cannot be null");
        final Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("route", routeId);
        final SearchResult<SalesOrderDeliverySLAModel> result = flexibleSearchService.search(FIND_SALES_ORDER_DELIVERY_SLA_BY_ROUTE,params);
        if(CollectionUtils.isNotEmpty(result.getResult()) && result.getResult().size()>0){
            return result.getResult().get(0);
        }
        else {
            return null;
        }
    }
}
