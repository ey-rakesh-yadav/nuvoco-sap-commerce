package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.DeliveryModeDao;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryModeDaoImpl implements DeliveryModeDao {


    @Autowired
    FlexibleSearchService flexibleSearchService;

    /**
     * @param code
     * @return
     */
    @Override
    public List<DeliveryModeModel> findDeliveryModesByCode(String code) {
        final StringBuilder query = new StringBuilder("SELECT {").append(DeliveryModeModel.PK);
        query.append("} FROM {").append(DeliveryModeModel._TYPECODE);
        query.append("} WHERE {").append(DeliveryModeModel.CODE).append("}=?").append(DeliveryModeModel.CODE);
        query.append(" ORDER BY {").append(DeliveryModeModel.CREATIONTIME).append("} DESC");
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(DeliveryModeModel.CODE, code);
        final List<DeliveryModeModel> deliveryModes = doSearch(query.toString(), params, null);
        return deliveryModes;
    }


    private <T> List<T> doSearch(final String query, final Map<String, Object> params, final List<Class> resultClasses)
    {
        final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
        if (params != null)
        {
            fQuery.addQueryParameters(params);
        }
        if (resultClasses != null)
        {
            fQuery.setResultClassList(resultClasses);
        }
        final SearchResult<T> result = flexibleSearchService.search(fQuery);
        final List<T> elements = result.getResult();
        return elements;
    }
}
