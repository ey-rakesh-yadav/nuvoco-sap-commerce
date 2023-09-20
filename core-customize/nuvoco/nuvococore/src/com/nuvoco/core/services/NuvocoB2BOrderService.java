package com.nuvoco.core.services;

import com.nuvoco.core.model.DeliverySlotMasterModel;
import com.nuvoco.facades.data.DeliveryDateAndSlotListData;
import com.nuvoco.facades.data.EpodFeedbackData;
import com.nuvoco.facades.data.NuvocoOrderHistoryData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.time.LocalDateTime;
import java.util.List;

public interface NuvocoB2BOrderService extends B2BOrderService  {


    Boolean getOrderFromRetailersRequest(String requisitionId, String status);

    SearchPageData<NuvocoOrderHistoryData> getOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter, String productName , String orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending);

    String validateAndMapOrderStatuses(final String inputStatus);

    SearchPageData<NuvocoOrderHistoryData> getOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, String spApprovalFilter);

    Boolean getEpodFeedback(EpodFeedbackData epodFeedbackData);

    SearchPageData<NuvocoOrderHistoryData> getEpodListBasedOnOrderStatus(SearchPageData searchPageData, List<String> Status, String filter);


    SearchPageData<NuvocoOrderHistoryData> getCancelOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, String spApprovalFilter, Integer month, Integer year);

    SearchPageData<NuvocoOrderHistoryData> getCancelOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, String spApprovalFilter, Integer month, Integer year);

    void updateTotalQuantity(long quantity);

    DeliveryDateAndSlotListData getOptimalDeliveryWindow(double orderQuantity, String routeId, B2BCustomerModel user,
                                                         LocalDateTime orderPunchedDate, String sourceCode, String isDealerProvidingTruck);


    List<DeliverySlotMasterModel> getDeliverySlotList();

}
