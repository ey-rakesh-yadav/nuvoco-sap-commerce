package com.nuvoco.core.services;


import com.nuvoco.facades.data.NuvocoDealerSalesAllocationData;

public interface DealerService {


    NuvocoDealerSalesAllocationData getStockAllocationForRetailer(String productCode);
    NuvocoDealerSalesAllocationData getStockAllocationForDealer(String productCode);
}
