package com.nuvoco.facades.impl;

import com.nuvoco.core.services.NuvocoB2BOrderService;
import com.nuvoco.facades.NuvocoB2BOrderfacade;
import com.nuvoco.facades.data.DeliveryDateAndSlotListData;
import com.nuvoco.facades.data.EpodFeedbackData;
import com.nuvoco.facades.data.NuvocoOrderData;
import com.nuvoco.facades.data.NuvocoOrderHistoryData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2bacceleratorfacades.order.impl.DefaultB2BOrderFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

public class NuvocoB2BOrderfacadeImpl extends DefaultB2BOrderFacade implements NuvocoB2BOrderfacade {

    private static final Logger LOGGER = Logger.getLogger(NuvocoB2BOrderfacadeImpl.class);

    @Autowired
    private NuvocoB2BOrderService nuvocoB2BOrderService;

    @Autowired
    private Converter<OrderModel, CartData> nuvocoOrderConverter;
    @Autowired
    private Converter<OrderEntryModel, NuvocoOrderData> nuvocoOrderEntryCardConverter;

    @Autowired
    private Converter<OrderModel, NuvocoOrderData> nuvocoOrderCardConverter;

    /**
     * @param requisitionId
     * @param status
     * @return
     */
    @Override
    public Boolean getOrderFromRetailersRequest(String requisitionId, String status) {
        return nuvocoB2BOrderService.getOrderFromRetailersRequest(requisitionId, status);
    }

    /**
     * @param searchPageData
     * @param orderStatus
     * @param filter
     * @param productName
     * @param orderType
     * @param isCreditLimitBreached
     * @param spApprovalFilter
     * @param approvalPending
     * @return
     */
    @Override
    public SearchPageData<NuvocoOrderHistoryData> getOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending) {
        return nuvocoB2BOrderService.getOrderHistoryForOrder(searchPageData,orderStatus,filter,productName,orderType,isCreditLimitBreached,spApprovalFilter,approvalPending);
    }

    /**
     * @param searchPageData
     * @param orderStatus
     * @param filter
     * @param productCode
     * @param orderType
     * @param spApprovalFilter
     * @return
     */
    @Override
    public SearchPageData<NuvocoOrderHistoryData> getOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter, String productCode, String orderType, String spApprovalFilter) {
        return nuvocoB2BOrderService.getOrderHistoryForOrderEntry(searchPageData,orderStatus,filter,productCode,orderType,spApprovalFilter);
    }

    /**
     * @param epodFeedbackData
     * @return
     */
    @Override
    public Boolean getEpodFeedback(EpodFeedbackData epodFeedbackData) {
        return nuvocoB2BOrderService.getEpodFeedback(epodFeedbackData);
    }

    /**
     * @param searchPageData
     * @param Status
     * @param filter
     * @return
     */
    @Override
    public SearchPageData<NuvocoOrderHistoryData> getEpodListBasedOnOrderStatus(SearchPageData searchPageData, List<String> Status, String filter) {
        return nuvocoB2BOrderService.getEpodListBasedOnOrderStatus(searchPageData,Status,filter);
    }

    /**
     * @param searchPageData
     * @param orderStatus
     * @param filter
     * @param productName
     * @param orderType
     * @param spApprovalFilter
     * @param month
     * @param year
     * @return
     */
    @Override
    public SearchPageData<NuvocoOrderHistoryData> getCancelOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, String spApprovalFilter, Integer month, Integer year) {
        return nuvocoB2BOrderService.getCancelOrderHistoryForOrder(searchPageData,orderStatus,filter,productName,orderType,spApprovalFilter,month,year);
    }

    /**
     * @param searchPageData
     * @param orderStatus
     * @param filter
     * @param productName
     * @param orderType
     * @param spApprovalFilter
     * @param month
     * @param year
     * @return
     */
    @Override
    public SearchPageData<NuvocoOrderHistoryData> getCancelOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, String spApprovalFilter, Integer month, Integer year) {
        return nuvocoB2BOrderService.getCancelOrderHistoryForOrderEntry(searchPageData,orderStatus,filter,productName,orderType,spApprovalFilter, month, year);
    }

    /**
     * @param orderCode
     * @param entryNumber
     * @return
     */
    @Override
    public NuvocoOrderData getOrderDetails(String orderCode, String entryNumber) {
        OrderModel orderModel = nuvocoB2BOrderService.getOrderForCode(orderCode);
        if(StringUtils.isNotBlank(entryNumber)){
            int entryNumberInt = 0;
            try {
                entryNumberInt = Integer.parseInt(entryNumber);
            }
            catch (Exception ex){
                LOGGER.debug("Error While Parsing : "+entryNumber);
                throw new ClassCastException("Could not parse entry number : "+entryNumber);
            }

            final OrderEntryModel orderEntry = nuvocoB2BOrderService.getEntryForNumber(orderModel,entryNumberInt);
            return nuvocoOrderEntryCardConverter.convert(orderEntry);
        }
        else{
            return nuvocoOrderCardConverter.convert(orderModel);
        }
    }

    /**
     * @param quantity
     */
    @Override
    public void updateTotalQuantity(long quantity) {
        nuvocoB2BOrderService.updateTotalQuantity(quantity);
    }

    /**
     * @param code
     * @return
     */
    @Override
    public CartData getOrderForCode(String code) {
        OrderModel orderModel = nuvocoB2BOrderService.getOrderForCode(code);
        CartData cartData = nuvocoOrderConverter.convert(orderModel);
        return cartData;
    }

    /**
     * @param orderQtyfinal
     * @param routeId
     * @param userId
     * @param sourceCode
     * @param isDealerProvidingTruck
     * @return
     */
    @Override
    public DeliveryDateAndSlotListData fetchOptimalDeliveryWindow(double orderQtyfinal, String routeId, String userId, String sourceCode, String isDealerProvidingTruck) {
        B2BCustomerModel customerModel = (B2BCustomerModel) getUserService().getUserForUID(userId);
        DeliveryDateAndSlotListData deliverySlots = nuvocoB2BOrderService.getOptimalDeliveryWindow(orderQtyfinal,routeId,customerModel, LocalDateTime.now(), sourceCode, isDealerProvidingTruck);
        return deliverySlots;
    }
}
