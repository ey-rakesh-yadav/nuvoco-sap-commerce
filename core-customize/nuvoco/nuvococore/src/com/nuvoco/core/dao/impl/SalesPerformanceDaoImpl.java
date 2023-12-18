package com.nuvoco.core.dao.impl;

import com.nuvoco.core.dao.SalesPerformanceDao;
import com.nuvoco.core.enums.RequisitionStatus;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.utility.NuvocoDateUtility;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class SalesPerformanceDaoImpl implements SalesPerformanceDao {


    @Autowired
    private FlexibleSearchService flexibleSearchService;

    @Autowired
    CatalogVersionService catalogVersionService;

    @Autowired
    ProductService productService;

    @Autowired
    BaseSiteService baseSiteService;

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

    /**
     * @param customer
     * @param baseSite
     * @param month
     * @param year
     * @param bgpFilter
     * @return
     */
    @Override
    public Double getActualTargetSalesForSelectedMonthAndYearForDealer(NuvocoCustomerModel customer, BaseSiteModel baseSite, int month, int year, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN NuvocoCustomer as sc on {o:user}={sc:pk}}  WHERE "
                    + "{o:user} = ?nuvocoCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and ").append(NuvocoDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN NuvocoCustomer as sc on {o:user}={sc:pk}}  WHERE "
                    + "{o:user} = ?nuvocoCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and ").append(NuvocoDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));

        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN NuvocoCustomer as sc on {o:user}={sc:pk} JOIN Product as p on {oe:product}={p:pk}}  WHERE "
                        + "{o:user} = ?nuvocoCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:product} =?product and").append(NuvocoDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));
                params.put("product", product);
            }
        }

        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("customer", customer);
        params.put("site", baseSite);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);

        // final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        //searchRestrictionService.disableSearchRestrictions();
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        //searchRestrictionService.enableSearchRestrictions();
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
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
