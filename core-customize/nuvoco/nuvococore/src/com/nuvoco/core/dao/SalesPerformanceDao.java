package com.nuvoco.core.dao;

import com.nuvoco.core.model.NuvocoCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

public interface SalesPerformanceDao {

    NuvocoCustomerModel getRetailerSalesForDealer(NuvocoCustomerModel customer, BaseSiteModel brand);

    Double getActualTargetSalesForSelectedMonthAndYearForDealer(NuvocoCustomerModel sclCustomer, BaseSiteModel baseSite, int month, int year,String bgpFilter);



}
