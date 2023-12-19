package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.OrderValidationProcessDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderValidationProcessDaoImpl implements OrderValidationProcessDao {


    private static final String DEFAULT_QUERY_PENDING_AMOUNT ="SELECT SUM({oe.totalPrice}) FROM {OrderEntry AS oe JOIN Order AS o ON {o.pk}={oe.order} } WHERE {o.user}=?userId AND ({o.erpOrderNumber} is not null or {o.orderValidatedDate} is not null or {o.orderSentForApprovalDate} is not null) AND {oe.invoiceCreationDateAndTime} is null AND {oe.cancelledDate} is null ";
    @Autowired
    private FlexibleSearchService flexibleSearchService;
    /**
     * @param userId
     * @return
     */
    @Override
    public Double getPendingOrderAmount(String userId) {
        final StringBuilder builder = new StringBuilder(DEFAULT_QUERY_PENDING_AMOUNT);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(Double.class));

        final SearchResult<Double > searchResult = flexibleSearchService.search(query);
        List<Double> result = searchResult.getResult();

        if(result!=null && !result.isEmpty())
            return result.get(0)!=null ? result.get(0) : 0.0;

        return 0.0;
    }
}
