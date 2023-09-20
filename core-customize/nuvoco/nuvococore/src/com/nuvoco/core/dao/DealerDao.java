package com.nuvoco.core.dao;

import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.ReceiptAllocaltionModel;
import com.nuvoco.core.model.RetailerRecAllocateModel;
import de.hybris.platform.core.model.product.ProductModel;

import java.util.List;

public interface DealerDao {

    List<List<Integer>>  getDealerTotalAllocation(NuvocoCustomerModel dealerCode);

    ReceiptAllocaltionModel getDealerAllocation(ProductModel productCode, NuvocoCustomerModel dealerCode);

    RetailerRecAllocateModel getRetailerAllocation(ProductModel productCode, NuvocoCustomerModel dealerCode);


}
