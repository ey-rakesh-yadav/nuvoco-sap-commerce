package com.nuvoco.core.services.impl;

import com.nuvoco.core.dao.DealerDao;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.ReceiptAllocaltionModel;
import com.nuvoco.core.services.DealerService;
import com.nuvoco.facades.CreditLimitData;
import com.nuvoco.facades.data.NuvocoDealerSalesAllocationData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class DealerServiceImpl implements DealerService {

    @Autowired
    private UserService userService;

    @Autowired
    ProductService productService;

    @Autowired
    DealerDao dealerDao;



    /**
     * @param productCode
     * @return
     */
    @Override
    public NuvocoDealerSalesAllocationData getStockAllocationForDealer(String productCode) {
        NuvocoDealerSalesAllocationData dealerAllocationData = new NuvocoDealerSalesAllocationData();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        NuvocoCustomerModel dealerModel = (NuvocoCustomerModel) currentUser;
        //Check for Product code is null then always return the total values of stocks for dealer available for all products
        if (dealerModel != null) {
            if (productCode == null) {
                List<List<Integer>> stockAvailList = dealerDao.getDealerTotalAllocation(dealerModel);
                //Always it should return one row with total stocks available
                List<Integer> totalStocksAvail = (stockAvailList != null)? stockAvailList.get(0): new ArrayList<Integer>();
                if (null != totalStocksAvail && !totalStocksAvail.isEmpty()) {
                    dealerAllocationData.setStockAvailableForInfluencer(totalStocksAvail.get(0));
                    dealerAllocationData.setStockAvailableForRetailer(totalStocksAvail.get(1));
                }
            } else {
                //When request for each product code and each dealer received
                ProductModel productModel = productService.getProductForCode(productCode);
                if (null != productModel) {
                    ReceiptAllocaltionModel receiptAllocation = dealerDao.getDealerAllocation(productModel, dealerModel);
                    if (receiptAllocation != null) {
                        dealerAllocationData.setProductCode(productCode);
                        dealerAllocationData.setStockAvailableForInfluencer(receiptAllocation.getStockAvlForInfluencer());
                        dealerAllocationData.setStockAvailableForRetailer(receiptAllocation.getStockAvlForRetailer());
                    }
                }
            }
        }

        return dealerAllocationData;
    }
}
