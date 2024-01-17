package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.CollectionDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CollectionDaoImpl implements CollectionDao {


    @Autowired
    FlexibleSearchService flexibleSearchService;
    /**
     * @param customerNo
     * @return
     */
    @Override
    public Double getDailyAverageSalesForDealer(String customerNo) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {dailyAverageSales} FROM {CreditAndOutstanding} WHERE {customerCode}=?customerNo");
        params.put("customerNo", customerNo);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }
}
