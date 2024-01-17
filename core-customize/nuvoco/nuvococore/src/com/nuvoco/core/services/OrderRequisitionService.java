package com.nuvoco.core.services;

import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.OrderRequisitionModel;
import com.nuvoco.facades.data.OrderRequisitionData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;

public interface OrderRequisitionService {


    boolean saveOrderRequisitionDetails(OrderRequisitionData orderRequisitionData);
    SearchPageData<OrderRequisitionModel> getOrderRequisitionDetails(List<String> statuses, String submitType, Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, NuvocoCustomerModel currentUser, String productCode, SearchPageData searchPageData, String requisitionId, String searchKey);

    Boolean updateOrderRequistionStatus(String requisitionId, String status, Double receivedQty, String cancelReason);
    void saveDealerRetailerMapping(NuvocoCustomerModel dealer, NuvocoCustomerModel retailer, BaseSiteModel brand);

    void orderCountIncrementForDealerRetailerMap(Date deliveredDate, NuvocoCustomerModel dealer, NuvocoCustomerModel retailer, BaseSiteModel brand);

}
