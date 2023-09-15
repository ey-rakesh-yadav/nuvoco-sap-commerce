package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.OrderRequisitionDao;
import com.nuvoco.core.enums.RequisitionStatus;
import com.nuvoco.core.model.NuvocoCustomerModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import javax.annotation.Resource;
import java.util.*;

public class OrderRequisitionDaoImpl implements OrderRequisitionDao {

    @Resource
    FlexibleSearchService flexibleSearchService;

    /**
     * @param toCustomerList
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<List<Object>> getSalsdMTDforRetailer(List<NuvocoCustomerModel> toCustomerList, String startDate, String endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {p.toCustomer},sum({p.quantity}) from {OrderRequisition as p} ")
                .append(" where {p.deliveredDate}>=?startDate and {p.deliveredDate}<=?endDate ")
                .append(" and {p.toCustomer} in (?toCustomerList) and {p:status}=?status group by {p.toCustomer} ");
        params.put("toCustomerList", toCustomerList);
        params.put("status", RequisitionStatus.DELIVERED);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(NuvocoCustomerModel.class, Double.class));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        List<List<Object>> result = searchResult.getResult();
        return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }
}
