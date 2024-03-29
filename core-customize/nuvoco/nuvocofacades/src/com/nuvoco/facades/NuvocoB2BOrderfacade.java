package com.nuvoco.facades;

import com.nuvoco.facades.data.DeliveryDateAndSlotListData;
import com.nuvoco.facades.data.EpodFeedbackData;
import com.nuvoco.facades.data.NuvocoOrderData;
import com.nuvoco.facades.data.NuvocoOrderHistoryData;
import de.hybris.platform.b2bacceleratorfacades.order.B2BOrderFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface NuvocoB2BOrderfacade extends B2BOrderFacade {

    Boolean getVehicleArrivalConfirmationForOrder(boolean vehicleArrived, String orderCode, String entryNumber);


    Boolean updateEpodStatusForOrder(double shortageQuantity, String orderCode, int entryNumber);

    Boolean getOrderFromRetailersRequest(String requisitionId, String status);

    SearchPageData<NuvocoOrderHistoryData> getOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending);


    SearchPageData<NuvocoOrderHistoryData> getOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter,String productCode,String orderType, String spApprovalFilter);

    Boolean getEpodFeedback(EpodFeedbackData epodFeedbackData);

    SearchPageData<NuvocoOrderHistoryData> getEpodListBasedOnOrderStatus(SearchPageData searchPageData, List<String> Status, String filter);


    SearchPageData<NuvocoOrderHistoryData> getCancelOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter,String productName,String orderType, String spApprovalFilter, Integer month, Integer year);

    SearchPageData<NuvocoOrderHistoryData> getCancelOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter,String productName,String orderType, String spApprovalFilter, Integer month, Integer year);

    NuvocoOrderData getOrderDetails(final String orderCode , final String entryNumber);

    void updateTotalQuantity(long quantity);

    CartData getOrderForCode(String code);

    DeliveryDateAndSlotListData fetchOptimalDeliveryWindow(final double orderQtyfinal, final String routeId , final String userId, final String sourceCode, String isDealerProvidingTruck);

}
