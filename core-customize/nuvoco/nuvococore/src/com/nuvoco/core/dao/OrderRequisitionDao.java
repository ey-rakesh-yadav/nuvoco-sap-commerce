package com.nuvoco.core.dao;

import com.nuvoco.core.model.DealerRetailerMapModel;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.OrderRequisitionModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface OrderRequisitionDao {

    List<List<Object>> getSalsdMTDforRetailer(List<NuvocoCustomerModel> toCustomerList, String startDate, String endDate);
    public OrderRequisitionModel findByRequisitionId(String requisitionId);

    OrderModel findOrderByCode(String code);
    DealerRetailerMapModel getDealerforRetailerDetails(NuvocoCustomerModel dealer, NuvocoCustomerModel retailer, BaseSiteModel brand);
    SearchPageData<OrderRequisitionModel> getOrderRequisitionDetails(List<String> status, String submitType, String fromDate, String toDate, NuvocoCustomerModel currentUser, String productCode, SearchPageData searchPageData, String requisitionId, String searchKey);
}
