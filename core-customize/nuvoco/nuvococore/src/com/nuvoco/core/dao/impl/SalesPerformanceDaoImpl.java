package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.SalesPerformanceDao;
import com.nuvoco.core.enums.RequisitionStatus;
import com.nuvoco.core.model.NuvocoCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SalesPerformanceDaoImpl implements SalesPerformanceDao {


    @Autowired
    private FlexibleSearchService flexibleSearchService;
    /**
     * @param customer
     * @param brand
     * @return
     */
    @Override
    public NuvocoCustomerModel getRetailerSalesForDealer(NuvocoCustomerModel customer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //select {d.retailer} from {DealerRetailerMap as d join SclCustomer as c  on {d.retailer}={c.pk}} WHERE {d.dealer}=?sclCustomer AND {d.active}=?active AND {d.brand}=?brand
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:retailer}) from {DealerRetailerMap as sc LEFT JOIN OrderRequisition AS oe on {sc:retailer}={oe:toCustomer}} " +
                "WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?customer and ").append(getMtdClauseQuery("oe:deliveredDate", params));
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("customer", customer);
        params.put("brand", brand);
        params.put("requisitionStatus", requisitionStatus);
        /*final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<DealerRetailerMapModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();*/
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.setResultClassList(Collections.singletonList(NuvocoCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<NuvocoCustomerModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() !=null && !searchResult.getResult().isEmpty()? searchResult.getResult().get(0) : null;

    }

    private static String getMtdClauseQuery(String columnName, Map<String, Object> map) {
        LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
        return getDateQuery(columnName, firstDayOfMonth, lastDayOfMonth, map);
    }

    private static String getDateQuery(String columnName, LocalDate firstDayOfMonth, LocalDate lastDayOfMonth, Map<String, Object> map) {
        StringBuilder builder = new StringBuilder();
        builder.append(" {").append(columnName)
                .append("} >= ?start and {").append(columnName).append("} < ?end ");

        map.put("start", getStartDateConstraint(firstDayOfMonth));
        map.put("end", getStartDateConstraint(lastDayOfMonth));

        return builder.toString();
    }

    public static Date getStartDateConstraint(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = localDate.atStartOfDay(zone);
        Date date = Date.from(dateTime.toInstant());
        return date;
    }

}
