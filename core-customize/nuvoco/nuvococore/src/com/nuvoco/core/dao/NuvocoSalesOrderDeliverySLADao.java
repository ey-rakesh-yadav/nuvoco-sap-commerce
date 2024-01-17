package com.nuvoco.core.dao;

import com.nuvoco.core.model.SalesOrderDeliverySLAModel;

public interface NuvocoSalesOrderDeliverySLADao {


    SalesOrderDeliverySLAModel findByRoute(String routeId);
}
