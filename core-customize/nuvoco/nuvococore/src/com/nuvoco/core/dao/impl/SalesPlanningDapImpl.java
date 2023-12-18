package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.SalesPlanningDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesPlanningDapImpl implements SalesPlanningDao {


    @Autowired
    FlexibleSearchService flexibleSearchService;

    /**
     * @param dealerUid
     * @param monthYear
     * @return
     */
    @Override
    public Double getDealerSalesAnnualTarget(String dealerUid, String monthYear) {
        final Map<String, Object> params = new HashMap<>();
        String queryString="select {mt.monthTarget} from {MonthWiseAnnualTarget as mt} where {mt.customerCode}=?customerCode and {mt.monthYear}=?monthYear and {mt.isAnnualSalesReviewedForDealer}=?truee and {mt.isAnnualSalesRevisedForDealer}=?falsee and {mt.retailerCode} is null and {mt.productCode} is null and {selfCounterCustomerCode} is null";
        boolean truee = Boolean.TRUE;
        boolean falsee = Boolean.FALSE;
        params.put("customerCode",dealerUid);
        params.put("monthYear",monthYear);
        params.put("truee",truee);
        params.put("falsee",falsee);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        query.setResultClassList(List.of(Double.class));
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }
}
