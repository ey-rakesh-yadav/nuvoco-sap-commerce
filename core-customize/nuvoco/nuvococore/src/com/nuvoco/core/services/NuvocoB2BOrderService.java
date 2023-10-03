package com.nuvoco.core.services;

import com.nuvoco.core.model.DeliverySlotMasterModel;
import com.nuvoco.facades.data.DeliveryDateAndSlotListData;
import com.nuvoco.facades.data.EpodFeedbackData;
import com.nuvoco.facades.data.NuvocoOrderHistoryData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.time.LocalDateTime;
import java.util.List;

public interface NuvocoB2BOrderService extends B2BOrderService  {


    void getRequisitionStatusByOrderLines(OrderModel order);

    Boolean updateEpodStatusForOrder(double shortageQuantity, String orderCode, int entryNumber);

    Boolean getVehicleArrivalConfirmationForOrder(boolean vehicleArrived, String orderCode, String entryNumber);
    Boolean getOrderFromRetailersRequest(String requisitionId, String status);

    SearchPageData<NuvocoOrderHistoryData> getOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter, String productName , String orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending);

    String validateAndMapOrderStatuses(final String inputStatus);

    SearchPageData<NuvocoOrderHistoryData> getOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, String spApprovalFilter);

    Boolean getEpodFeedback(EpodFeedbackData epodFeedbackData);

    SearchPageData<NuvocoOrderHistoryData> getEpodListBasedOnOrderStatus(SearchPageData searchPageData, List<String> Status, String filter);


    SearchPageData<NuvocoOrderHistoryData> getCancelOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, String spApprovalFilter, Integer month, Integer year);

    SearchPageData<NuvocoOrderHistoryData> getCancelOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, String spApprovalFilter, Integer month, Integer year);

    AbstractOrderEntryModel addNewOrderEntry(final OrderModel order, final ProductModel product,
                                             final long qty, final UnitModel unit, final int number);


    void updateTotalQuantity(long quantity);

    DeliveryDateAndSlotListData getOptimalDeliveryWindow(double orderQuantity, String routeId, B2BCustomerModel user,
                                                         LocalDateTime orderPunchedDate, String sourceCode, String isDealerProvidingTruck);


    void saveOrderRequisitionEntryDetails(OrderModel order, OrderEntryModel orderEntry, String status);

    List<DeliverySlotMasterModel> getDeliverySlotList();

}
