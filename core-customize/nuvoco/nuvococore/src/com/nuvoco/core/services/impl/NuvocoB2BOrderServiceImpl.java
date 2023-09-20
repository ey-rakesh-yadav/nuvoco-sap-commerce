package com.nuvoco.core.services.impl;

import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.dao.*;
import com.nuvoco.core.enums.*;
import com.nuvoco.core.model.*;
import com.nuvoco.core.services.NuvocoB2BOrderService;
import com.nuvoco.core.services.OrderRequisitionService;
import com.nuvoco.facades.data.DeliveryDateAndSlotData;
import com.nuvoco.facades.data.DeliveryDateAndSlotListData;
import com.nuvoco.facades.data.EpodFeedbackData;
import com.nuvoco.facades.data.NuvocoOrderHistoryData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.b2b.services.impl.DefaultB2BOrderService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.*;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.order.CartService;

import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import de.hybris.platform.servicelayer.model.ModelService;

import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class NuvocoB2BOrderServiceImpl extends DefaultB2BOrderService implements NuvocoB2BOrderService {



    private static final Logger LOGGER = Logger.getLogger(NuvocoB2BOrderServiceImpl.class);
    @Autowired
    BaseStoreService baseStoreService;
    @Autowired
    private UserService userService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private FlexibleSearchService flexibleSearchService;

    @Autowired
    Converter<OrderModel, NuvocoOrderHistoryData> nuvocoOrderHistoryCardConverter;

    @Autowired
    Converter<OrderEntryModel, NuvocoOrderHistoryData> nuvocoOrderEntryHistoryCardConverter;
    @Autowired
    private NuvocoOrderCountDao nuvocoOrderCountDao;

    @Autowired
    private ModelService modelService;

    @Autowired
    private CartService cartService;

    @Autowired
    private DeliverySlotMasterDao deliverySlotMasterDao;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private NuvocoSalesOrderDeliverySLADao nuvocoSalesOrderDeliverySLADao;

    @Autowired
    OrderRequisitionDao orderRequisitionDao;

    @Autowired
    DealerDao dealerDao;

    @Autowired
    OrderRequisitionService orderRequisitionService;

    @Autowired
    BaseSiteService baseSiteService;

    /**
     * @param order
     */
    @Override
    public void getRequisitionStatusByOrderLines(OrderModel order) {

        int orderLineCancelledCount = 0;
        int orderLineDeliveredCount = 0;
        Date partialDeliveredDate = null;

        if(order.getRequisitions()!=null && !order.getRequisitions().isEmpty() && order.getRequisitions().size()==1) {
            OrderRequisitionModel orderRequisitionModel = order.getRequisitions().get(0);
            for(AbstractOrderEntryModel entryModel : order.getEntries()) {
                if(entryModel.getCancelledDate()!=null) {
                    orderLineCancelledCount += 1;
                }
                else if(entryModel.getDeliveredDate()!=null) {
                    orderLineDeliveredCount += 1;
                    partialDeliveredDate = entryModel.getDeliveredDate();
                }
            }

            int orderLineCancelledAndDeliveredCount = orderLineDeliveredCount + orderLineCancelledCount;
            if(orderLineCancelledAndDeliveredCount >= order.getEntries().size()) {
                if(orderLineCancelledCount  >= 1 && orderLineDeliveredCount >= 1) {
                    orderRequisitionModel.setStatus(RequisitionStatus.PARTIAL_DELIVERED);
                    orderRequisitionModel.setPartialDeliveredDate(partialDeliveredDate);
                    orderRequisitionModel.setDeliveredDate(partialDeliveredDate);
                    orderRequisitionService.orderCountIncrementForDealerRetailerMap(orderRequisitionModel.getDeliveredDate(),orderRequisitionModel.getFromCustomer(), orderRequisitionModel.getToCustomer(), baseSiteService.getCurrentBaseSite());
                }

                if(orderLineCancelledCount == order.getEntries().size()) {
                    orderRequisitionModel.setStatus(RequisitionStatus.CANCELLED);
                }
                getModelService().save(orderRequisitionModel);
            }

        }
    }

    /**
     * @param shortageQuantity
     * @param orderCode
     * @param entryNumber
     * @return
     */
    @Override
    public Boolean updateEpodStatusForOrder(double shortageQuantity, String orderCode, int entryNumber) {
        OrderModel order = getOrderForCode(orderCode);
        OrderEntryModel orderEntry = (OrderEntryModel) order.getEntries().stream().filter(abstractOrderEntryModel -> abstractOrderEntryModel.getEntryNumber()==entryNumber).findAny().get();
        orderEntry.setShortageQuantity(shortageQuantity);

        if(shortageQuantity>0){
            orderEntry.setEpodStatus(EpodStatus.DISPUTED);
        }
        else {
            orderEntry.setEpodStatus(EpodStatus.APPROVED);
        }
        orderEntry.setStatus(OrderStatus.DELIVERED);
        orderEntry.setDeliveredDate(new Date());
        orderEntry.setEpodCompleted(true);
        orderEntry.setEpodCompletedDate(new Date());
        getModelService().save(orderEntry);

        saveOrderRequisitionEntryDetails(order, orderEntry, "EPOD");

        return true;
    }

    /**
     * @param vehicleArrived
     * @param orderCode
     * @param entryNumber
     * @return
     */
    @Override
    public Boolean getVehicleArrivalConfirmationForOrder(boolean vehicleArrived, String orderCode, String entryNumber) {
        OrderModel order = getOrderForCode(orderCode);
        int entryNum = Integer.valueOf(entryNumber);
        OrderEntryModel orderEntry = (OrderEntryModel) order.getEntries().stream().filter(abstractOrderEntryModel -> abstractOrderEntryModel.getEntryNumber()==entryNum).findAny().get();
        Date date = new Date();
        if(vehicleArrived == true){
            orderEntry.setIsVehicleArrived(true);
            orderEntry.setStatus(OrderStatus.EPOD_PENDING);
            orderEntry.setEpodInitiateDate(date);
            orderEntry.setEpodStatus(EpodStatus.PENDING);
            getModelService().save(orderEntry);
        }
        else{
            //TODO: Dependent on other persona
        }
        return true;
    }

    /**
     * @param requisitionId
     * @param status
     * @return
     */
    @Override
    public Boolean getOrderFromRetailersRequest(String requisitionId, String status) {
        OrderRequisitionModel model = orderRequisitionDao.findByRequisitionId(requisitionId);

        if(status!=null && status.equals("SELF_STOCK")){
            model.setStatus(RequisitionStatus.DELIVERED);
            model.setServiceType(ServiceType.SELF_STOCK);
            LOGGER.info("1. Stock allocation on getOrderFromRetailersRequest--> on SELF STOCK-->> qty="
                    + model.getQuantity() + ":::Requisition Id:::" + requisitionId);
            if (null != model
                    && null != model.getProduct() && null != model.getFromCustomer() && null != model.getToCustomer()) {
                LOGGER.info("2. Stock allocation --> Before getting DealerAllocation::: Product:::" + model.getProduct() + ":::Dealer Code:::"
                        + model.getFromCustomer());
                ReceiptAllocaltionModel receiptAllocate = dealerDao.getDealerAllocation(model.getProduct(),model.getFromCustomer());
                RetailerRecAllocateModel receiptRetailerAllocate = dealerDao.getRetailerAllocation(model.getProduct(),model.getToCustomer());
                Double updatedQty = 0.0;
                int stockAvailableForRetailer = 0;
                int stockAvailableForInfluencer = 0;
                Integer orderRequisitionQtyToUpdate = model.getQuantity().intValue();

                if (null != receiptAllocate) {
                    LOGGER.info("3. Stock allocation Record found--- avl qty="
                            + model.getQuantity() + " Available receipt stock-->" + receiptAllocate.getReceipt()
                            + " Available stock for retailer -->" + receiptAllocate.getStockAvlForRetailer()
                         /*   + " Available stock for influencer -->" + receiptAllocate.getStockAvlForInfluencer()*/);
                    int receiptQty = 0;
                    if (null != receiptAllocate.getReceipt()) {
                        receiptQty = receiptAllocate.getReceipt();
                    }
                    Integer qtyToUpdate = receiptQty - orderRequisitionQtyToUpdate;
                    LOGGER.info("4. Stock allocation Record found--- qtyToUpdate=" + qtyToUpdate);
                    if(qtyToUpdate > 0) {
                        //receiptAllocate.setReceipt(qtyToUpdate);
                        updatedQty = receiptAllocate.getSalesToRetailer() + model.getQuantity();
                        receiptAllocate.setSalesToRetailer(updatedQty.intValue());
                        stockAvailableForRetailer = getStockAvailForRetailer(receiptAllocate.getReceipt(),
                                updatedQty.intValue(), receiptAllocate.getSalesToInfluencer());
                        stockAvailableForInfluencer = getStockAvailForInfluencer(receiptAllocate.getReceipt(),
                                receiptAllocate.getSalesToRetailer(), receiptAllocate.getSalesToInfluencer());
                      //  receiptAllocate.setStockAvlForInfluencer(stockAvailableForInfluencer);
                        receiptAllocate.setStockAvlForRetailer(stockAvailableForRetailer);
                        LOGGER.info("5. Stock allocation Record found--- After updating--"
                                + " Available Receipt stock-->" + receiptAllocate.getReceipt()
                           /*     + " Available stock for retailer (reduce) -->" + receiptAllocate.getStockAvlForRetailer()
                                + " Available stock for influencer -->" + receiptAllocate.getStockAvlForInfluencer()*/);
                        getModelService().save(receiptAllocate);
                    }
                } else {
                    LOGGER.info("6. Stock allocation Stock allocation on getOrderFromRetailersRequest--> on SELF STOCK-->> "
                            + " New entry---OrderRequisition for Product or Dealer not found qty= ");
                    updatedQty = model.getQuantity();
                    //If product and dealer is not found in the RetailerRecAllocate
                    //then it means new entry has to be made as orderrequisition is placed with this combination
                    ReceiptAllocaltionModel receiptRetailerAllocateNew = getModelService().create(ReceiptAllocaltionModel.class);
                    receiptRetailerAllocateNew.setProduct(model.getProduct().getPk().toString());
                    receiptRetailerAllocateNew.setDealerCode(model.getFromCustomer().getPk().toString());
                    receiptRetailerAllocateNew.setReceipt((null != updatedQty || 0.0 <= updatedQty)?updatedQty.intValue():0);
                    receiptRetailerAllocateNew.setSalesToInfluencer(0);
                    receiptRetailerAllocateNew.setSalesToRetailer(0);
                    stockAvailableForRetailer = getStockAvailForRetailer(receiptAllocate.getReceipt(),
                            updatedQty.intValue(), receiptAllocate.getSalesToInfluencer());
                   /* stockAvailableForInfluencer = getStockAvailForInfluencer(receiptAllocate.getReceipt(),
                            receiptAllocate.getSalesToRetailer(), receiptAllocate.getSalesToInfluencer());
                    receiptAllocate.setStockAvlForInfluencer(stockAvailableForInfluencer);*/
                    receiptAllocate.setStockAvlForRetailer(stockAvailableForRetailer);
                    getModelService().save(receiptRetailerAllocateNew);
                    getModelService().refresh(receiptRetailerAllocateNew);
                }
                LOGGER.info("7. Stock allocation After update stocks values----- on SELF STOCK-->> qty="
                        + model.getQuantity());
                //Update for Retailer and Influencer as well
                if (null != receiptRetailerAllocate) {
                    LOGGER.info("8. Retailer RECEIPT:::Record found--- Receipts for Retailer when dealer self stock " + receiptRetailerAllocate.getReceipt()
                            + " Dealer No -->" + receiptRetailerAllocate.getDealerCode()
                          /*  + " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
                            + " Available allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer()*/);
                    updatedQty = receiptRetailerAllocate.getReceipt() + model.getQuantity();
                    receiptRetailerAllocate.setReceipt((null != updatedQty)?updatedQty.intValue():0);
//		  			receiptRetailerAllocate.setSalesToInfluencer((new Double(receivedQuantity * SclCoreConstants.QUANTITY_INMT_TO_BAGS)).intValue());
                    //int stockRetailerToInfluencer = (int) ((1.0 * (receiptRetailerAllocate.getReceipt()	- receiptRetailerAllocate.getSalesToInfluencer())));
                   // receiptRetailerAllocate.setStockAvlForInfluencer(stockRetailerToInfluencer);
                    LOGGER.info("9. Retailer RECEIPT:::Updated " + receiptRetailerAllocate.getReceipt()
                          /*  + " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()*/
                          /*  + " Allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer()*/);
                    modelService.save(receiptRetailerAllocate);
                } else {
                    //If product and dealer is not found in the RetailerRecAllocate
                    //then it means new entry has to be made as orderrequisition is placed with this combination
                    RetailerRecAllocateModel receiptRetailerAllocateNew = modelService.create(RetailerRecAllocateModel.class);
                    receiptRetailerAllocateNew.setProduct(model.getProduct().getPk().toString());
                    receiptRetailerAllocateNew.setDealerCode(model.getToCustomer().getPk().toString());
                    updatedQty = receiptRetailerAllocateNew.getReceipt() + model.getQuantity();
                    receiptRetailerAllocateNew.setReceipt((null != updatedQty)?updatedQty.intValue():0);
                    receiptRetailerAllocateNew.setSalesToInfluencer(0);
                    int stockRetailerInfluencer = (int) ((1.0 * (receiptRetailerAllocateNew.getReceipt() - receiptRetailerAllocateNew.getSalesToInfluencer())));
                    receiptRetailerAllocateNew.setStockAvlForInfluencer(stockRetailerInfluencer);
                    modelService.save(receiptRetailerAllocateNew);
                    modelService.refresh(receiptRetailerAllocateNew);
                }
            }
            Date date = new Date();
            if(model.getAcceptedDate()==null) {
                model.setAcceptedDate(date);
            }
            model.setFulfilledDate(date);
            model.setDeliveredDate(date);
            orderRequisitionService.orderCountIncrementForDealerRetailerMap(model.getDeliveredDate(),model.getFromCustomer(),model.getToCustomer(), baseSiteService.getCurrentBaseSite());
        } else if (status!=null && status.equals("REJECT_REQUEST")) {
            model.setStatus(RequisitionStatus.REJECTED);
            model.setRejectedDate(new Date());
            model.setRejectedBy((NuvocoCustomerModel) getUserService().getCurrentUser());
        }
        getModelService().save(model);

        return true;
    }


    private int getStockAvailForRetailer(int receipt, int saleToRetailer, int saleToInfluencer) {
        int stockRetailer = 0;
        stockRetailer = receipt - saleToRetailer - saleToInfluencer;
        return stockRetailer;
    }

    //To calculate the stock for Influencer when requisition placed
    private int getStockAvailForInfluencer(int receipt, int saleToRetailer, int saleToInfluencer) {
        int stockInfluencer = 0;
        stockInfluencer = (int) ((0.7 * (receipt - saleToRetailer)) - saleToInfluencer);
        return stockInfluencer;
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

        final UserModel currentUser = userService.getCurrentUser();
        String statues = validateAndMapOrderStatuses(orderStatus);
        final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
        final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
        final SearchPageData<NuvocoOrderHistoryData> result = new SearchPageData<>();

        if(StringUtils.isBlank(filter) && StringUtils.isBlank(productName) && StringUtils.isBlank(orderType)) {
            SearchPageData<OrderModel> ordersListByStatusForSO = nuvocoOrderCountDao.findOrdersListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, isCreditLimitBreached, spApprovalFilter, approvalPending);
            result.setPagination(ordersListByStatusForSO.getPagination());
            result.setSorts(ordersListByStatusForSO.getSorts());
            final List<NuvocoOrderHistoryData> orderHistoryData = nuvocoOrderHistoryCardConverter.convertAll(ordersListByStatusForSO.getResults());
            result.setResults(orderHistoryData);
        }
        else
        {
            OrderType orderTypeEnum = null;
            if(StringUtils.isNotBlank(orderType)){
                orderTypeEnum = OrderType.valueOf(orderType);
            }

            SearchPageData<OrderModel> ordersListByStatusForSO = nuvocoOrderCountDao.findOrdersListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, filter,productName,orderTypeEnum,isCreditLimitBreached, spApprovalFilter, approvalPending);
            result.setPagination(ordersListByStatusForSO.getPagination());
            result.setSorts(ordersListByStatusForSO.getSorts());
            final List<NuvocoOrderHistoryData> orderHistoryData = nuvocoOrderHistoryCardConverter.convertAll(ordersListByStatusForSO.getResults());
            result.setResults(orderHistoryData);
        }
        return result;
    }

    /**
     * @param inputStatus
     * @return
     */
    @Override
    public String validateAndMapOrderStatuses(String inputStatus) {
        String statuses;
        switch(inputStatus){
            case NuvocoCoreConstants.ORDER.PENDING_FOR_APPROVAL_STATUS:
                statuses = configurationService.getConfiguration().getString(NuvocoCoreConstants.ORDER.PENDING_FOR_APPROVAL_STATUS_MAPPING);
                break;

            case NuvocoCoreConstants.ORDER.VEHICLE_ARRIVAL_CONFIRMATION_STATUS:
                statuses = configurationService.getConfiguration().getString(NuvocoCoreConstants.ORDER.VEHICLE_ARRIVAL_CONFIRMATION_STATUS_MAPPING);
                break;

            case NuvocoCoreConstants.ORDER.WAITING_FOR_DISPATCH_STATUS:
                statuses = configurationService.getConfiguration().getString(NuvocoCoreConstants.ORDER.WAITING_FOR_DISPATCH_STATUS_MAPPING);
                break;

            case NuvocoCoreConstants.ORDER.TO_BE_DELIVERED_BY_TODAY_STATUS:
                statuses = configurationService.getConfiguration().getString(NuvocoCoreConstants.ORDER.TO_BE_DELIVERED_BY_TODAY_STATUS_MAPPING);
                break;

            case NuvocoCoreConstants.ORDER.ORDER_CANCELLATION_STATUS:
                statuses = configurationService.getConfiguration().getString(NuvocoCoreConstants.ORDER.ORDER_CANCELLATION_STATUS_MAPPING);
                break;

            case NuvocoCoreConstants.ORDER.ORDER_LINE_CANCELLATION_STATUS:
                statuses = configurationService.getConfiguration().getString(NuvocoCoreConstants.ORDER.ORDER_LINE_CANCELLATION_STATUS_MAPPING);
                break;

            default :
                statuses = inputStatus;
        }
        return statuses;
    }

    /**
     * @param searchPageData
     * @param orderStatus
     * @param filter
     * @param productName
     * @param orderType
     * @param spApprovalFilter
     * @return
     */
    @Override
    public SearchPageData<NuvocoOrderHistoryData> getOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, String spApprovalFilter) {
        final UserModel currentUser = userService.getCurrentUser();
        String statues = validateAndMapOrderStatuses(orderStatus);
        final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
        final Set<OrderStatus> statusSet = extractOrderStatuses(statues);

        final SearchPageData<NuvocoOrderHistoryData> result = new SearchPageData<>();

        if(StringUtils.isBlank(filter) && StringUtils.isBlank(productName) && StringUtils.isBlank(orderType))
        {
            SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = nuvocoOrderCountDao.findOrderEntriesListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, spApprovalFilter);
            result.setPagination(orderEntriesListByStatusForSO.getPagination());
            result.setSorts(orderEntriesListByStatusForSO.getSorts());
            List<NuvocoOrderHistoryData> orderHistoryData = nuvocoOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
            result.setResults(orderHistoryData);
        }
        else {
            OrderType orderTypeEnum = null;
            if(StringUtils.isNotBlank(orderType)){
                orderTypeEnum = OrderType.valueOf(orderType);
            }
            SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = nuvocoOrderCountDao.findOrderEntriesListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData,filter,productName,orderTypeEnum, spApprovalFilter);
            result.setPagination(orderEntriesListByStatusForSO.getPagination());
            result.setSorts(orderEntriesListByStatusForSO.getSorts());
            List<NuvocoOrderHistoryData> orderHistoryData = nuvocoOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
            result.setResults(orderHistoryData);
        }
        return result;
    }

    /**
     * @param epodFeedbackData
     * @return
     */
    @Override
    public Boolean getEpodFeedback(EpodFeedbackData epodFeedbackData) {
        Map<String,String> epodFeedback= new HashMap<>();

        String orderCode = epodFeedbackData.getOrderCode();
        int entryNumber = epodFeedbackData.getEntryNumber();

        OrderModel order = getOrderForCode(orderCode);
        OrderEntryModel orderEntry = (OrderEntryModel) order.getEntries().stream().filter(abstractOrderEntryModel -> abstractOrderEntryModel.getEntryNumber()==entryNumber).findAny().get();

        epodFeedback.put("driverRating",epodFeedbackData.getDriverRating());
        epodFeedback.put("deliveryProcess", epodFeedbackData.getDeliveryProcess());
        epodFeedback.put("materialReceipt", epodFeedbackData.getMaterialReceipt());
        epodFeedback.put("serviceLevel",epodFeedbackData.getServiceLevel());
        epodFeedback.put("overallDeliveryExperience", epodFeedbackData.getOverallDeliveryExperience());

        orderEntry.setEpodFeedback(epodFeedback);

        modelService.save(orderEntry);

        return true;
    }

    /**
     * @param searchPageData
     * @param Status
     * @param filter
     * @return
     */
    @Override
    public SearchPageData<NuvocoOrderHistoryData> getEpodListBasedOnOrderStatus(SearchPageData searchPageData, List<String> Status, String filter) {
        final UserModel currentUser = userService.getCurrentUser();
        final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();

        final SearchPageData<NuvocoOrderHistoryData> result = new SearchPageData<>();


        SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = nuvocoOrderCountDao.findOrderEntriesListByStatusForEPOD(currentUser, currentBaseStore, Status, searchPageData, filter);
        result.setPagination(orderEntriesListByStatusForSO.getPagination());
        result.setSorts(orderEntriesListByStatusForSO.getSorts());
        List<NuvocoOrderHistoryData> orderHistoryData = nuvocoOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
        result.setResults(orderHistoryData);

        return result;
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
        final UserModel currentUser = userService.getCurrentUser();
        String statues = validateAndMapOrderStatuses(orderStatus);
        final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
        final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
        final SearchPageData<NuvocoOrderHistoryData> result = new SearchPageData<>();

        String monthYear = null;
        if(month!=0 && year!=0) {
            int fYear = Integer.parseInt(year.toString());
            int fMonth = Integer.parseInt(month.toString());

            String singleDigitMonth = Integer.toString(fYear) + "-0" + Integer.toString(fMonth) + "-%";
            String doubleDigitMonth = Integer.toString(fYear) + "-" + Integer.toString(fMonth) + "-%";

            monthYear = (fMonth >= 1 && fMonth <= 9) ? singleDigitMonth : doubleDigitMonth;
        }

        if(StringUtils.isBlank(filter) && StringUtils.isBlank(productName) && StringUtils.isBlank(orderType)) {
            SearchPageData<OrderModel> ordersListByStatusForSO = nuvocoOrderCountDao.findCancelOrdersListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, spApprovalFilter, monthYear);
            result.setPagination(ordersListByStatusForSO.getPagination());
            result.setSorts(ordersListByStatusForSO.getSorts());
            final List<NuvocoOrderHistoryData> orderHistoryData = nuvocoOrderHistoryCardConverter.convertAll(ordersListByStatusForSO.getResults());
            result.setResults(orderHistoryData);
        }
        else
        {
            OrderType orderTypeEnum = null;
            if(StringUtils.isNotBlank(orderType)){
                orderTypeEnum = OrderType.valueOf(orderType);
            }

            SearchPageData<OrderModel> ordersListByStatusForSO = nuvocoOrderCountDao.findCancelOrdersListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, filter,productName,orderTypeEnum, spApprovalFilter, monthYear);
            result.setPagination(ordersListByStatusForSO.getPagination());
            result.setSorts(ordersListByStatusForSO.getSorts());
            final List<NuvocoOrderHistoryData> orderHistoryData = nuvocoOrderHistoryCardConverter.convertAll(ordersListByStatusForSO.getResults());
            result.setResults(orderHistoryData);
        }
        return result;
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
        final UserModel currentUser = userService.getCurrentUser();
        String statues = validateAndMapOrderStatuses(orderStatus);
        final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
        final Set<OrderStatus> statusSet = extractOrderStatuses(statues);

        final SearchPageData<NuvocoOrderHistoryData> result = new SearchPageData<>();

        String monthYear = null;
        if(month!=0 && year!=0) {
            int fYear = Integer.parseInt(year.toString());
            int fMonth = Integer.parseInt(month.toString());

            String singleDigitMonth = Integer.toString(fYear) + "-0" + Integer.toString(fMonth) + "-%";
            String doubleDigitMonth = Integer.toString(fYear) + "-" + Integer.toString(fMonth) + "-%";

            monthYear = (fMonth >= 1 && fMonth <= 9) ? singleDigitMonth : doubleDigitMonth;
        }

        if(StringUtils.isBlank(filter) && StringUtils.isBlank(productName) && StringUtils.isBlank(orderType))
        {
            SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = nuvocoOrderCountDao.findCancelOrderEntriesListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, spApprovalFilter, monthYear);
            result.setPagination(orderEntriesListByStatusForSO.getPagination());
            result.setSorts(orderEntriesListByStatusForSO.getSorts());
            List<NuvocoOrderHistoryData> orderHistoryData = nuvocoOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
            result.setResults(orderHistoryData);
        }
        else {
            OrderType orderTypeEnum = null;
            if(StringUtils.isNotBlank(orderType)){
                orderTypeEnum = OrderType.valueOf(orderType);
            }
            SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = nuvocoOrderCountDao.findCancelOrderEntriesListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData,filter,productName,orderTypeEnum, spApprovalFilter, monthYear);
            result.setPagination(orderEntriesListByStatusForSO.getPagination());
            result.setSorts(orderEntriesListByStatusForSO.getSorts());
            List<NuvocoOrderHistoryData> orderHistoryData = nuvocoOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
            result.setResults(orderHistoryData);
        }
        return result;
    }

    /**
     * @param quantity
     */
    @Override
    public void updateTotalQuantity(long quantity) {
        final CartModel cart = cartService.getSessionCart();
        cart.setTotalQuantity((double)quantity);
        modelService.save(cart);
        modelService.refresh(cart);
    }

    /**
     * @param orderQuantity
     * @param routeId
     * @param user
     * @param orderPunchedDate
     * @param sourceCode
     * @param isDealerProvidingTruck
     * @return
     */
    @Override
    public DeliveryDateAndSlotListData getOptimalDeliveryWindow(double orderQuantity, String routeId, B2BCustomerModel user, LocalDateTime orderPunchedDate, String sourceCode, String isDealerProvidingTruck) {
        DeliveryDateAndSlotListData list = new DeliveryDateAndSlotListData();
        List<DeliveryDateAndSlotData> dataList = new ArrayList<DeliveryDateAndSlotData>();
        final SalesOrderDeliverySLAModel salesOrderDeliverySLA = nuvocoSalesOrderDeliverySLADao.findByRoute(routeId);
        if(salesOrderDeliverySLA!=null) {

            WarehouseModel sourceMaster =  warehouseService.getWarehouseForCode(sourceCode);
            /////////////////////////////////////////
            if(sourceMaster!=null) {
                String sourceType = sourceMaster.getType().getCode();
                if(sourceType.equals("PLANT") || (sourceType.equals("DEPOT") && sourceMaster.getWorkingHourStartTime()!=null && sourceMaster.getWorkingHourEndTime()!=null)) {
                    int diCount = 0;
                    if(salesOrderDeliverySLA.getCommonTruckCapacity()!=null)
                        diCount = getDICountForOrder(salesOrderDeliverySLA.getCommonTruckCapacity(),orderQuantity);
                    int maxTruckCount = 1000;

                    LocalDateTime orderDateTime = orderPunchedDate;

                    double deliverySla=salesOrderDeliverySLA.getDeliverySlaHour();
                    double dispatchSla=salesOrderDeliverySLA.getDispatchSlaHour();

                    String noStart1 =salesOrderDeliverySLA.getNoEntryRestrictionStart1();
                    String noEnd1 = salesOrderDeliverySLA.getNoEntryRestrictionEnd1();
                    String noStart2 = salesOrderDeliverySLA.getNoEntryRestrictionStart2();
                    String noEnd2 = salesOrderDeliverySLA.getNoEntryRestrictionEnd2();
                    String noStart3 = salesOrderDeliverySLA.getNoEntryRestrictionStart3();
                    String noEnd3 = salesOrderDeliverySLA.getNoEntryRestrictionEnd3();
                    String noStart4 = salesOrderDeliverySLA.getNoEntryRestrictionStart4();
                    String noEnd4 = salesOrderDeliverySLA.getNoEntryRestrictionEnd4();

                    LocalTime localTimeNoStart1=null, localTimeNoEnd1=null, localTimeNoStart2=null, localTimeNoEnd2=null, localTimeNoStart3=null, localTimeNoEnd3=null, localTimeNoStart4=null, localTimeNoEnd4=null;

                    if(noStart1!=null && !noStart1.isBlank())
                        localTimeNoStart1 = LocalTime.parse(noStart1);
                    if(noEnd1!=null && !noEnd1.isBlank())
                        localTimeNoEnd1 = LocalTime.parse(noEnd1);
                    if(noStart2!=null && !noStart2.isBlank())
                        localTimeNoStart2 = LocalTime.parse(noStart2);
                    if(noEnd2!=null && !noEnd2.isBlank())
                        localTimeNoEnd2 = LocalTime.parse(noEnd2);
                    if(noStart3!=null && !noStart3.isBlank())
                        localTimeNoStart3 = LocalTime.parse(noStart3);
                    if(noEnd3!=null && !noEnd1.isBlank())
                        localTimeNoEnd3 = LocalTime.parse(noEnd3);
                    if(noStart4!=null && !noStart4.isBlank())
                        localTimeNoStart4 = LocalTime.parse(noStart4);
                    if(noEnd4!=null && !noEnd4.isBlank())
                        localTimeNoEnd4 = LocalTime.parse(noEnd4);



                    long slaInSeconds = (long) ((deliverySla+dispatchSla)*60*60);
                    if(sourceType.equals("DEPOT") && sourceMaster.getWorkingHourStartTime()!=null && sourceMaster.getWorkingHourEndTime()!=null) {
                        String workingHourStartTime = sourceMaster.getWorkingHourStartTime();
                        String workingHourEndTime = sourceMaster.getWorkingHourEndTime();

                        LocalTime localTimeWorkingStartTime = LocalTime.parse(workingHourStartTime);
                        LocalTime localTimeWorkingEndTime = LocalTime.parse(workingHourEndTime);

                        LocalDateTime workingStartDate;
                        LocalDateTime workingEndDate;
                        if(localTimeWorkingStartTime.isAfter(localTimeWorkingEndTime)) {
                            workingStartDate = LocalDateTime.of(orderDateTime.toLocalDate(), localTimeWorkingStartTime);
                            workingEndDate = LocalDateTime.of(orderDateTime.toLocalDate().plusDays(1), localTimeWorkingEndTime);
                        }
                        else {
                            workingStartDate = LocalDateTime.of(orderDateTime.toLocalDate(), localTimeWorkingStartTime);
                            workingEndDate = LocalDateTime.of(orderDateTime.toLocalDate(), localTimeWorkingEndTime);
                        }
                        LocalDateTime orderDispatchEndDate= orderDateTime.plusSeconds((long) (dispatchSla*60*60));
                        if(orderDispatchEndDate.isAfter(workingEndDate)) {
                            orderDateTime = workingStartDate.plusDays(1);
                        }
                    }

                    LocalDateTime orderWithSla = orderDateTime.plusSeconds(slaInSeconds);
                    LocalDate orderDate =orderWithSla.toLocalDate();

                    LocalDateTime tempPromisedDate = orderWithSla;

                    if(localTimeNoStart1!=null && localTimeNoEnd1!=null && localTimeNoStart1.compareTo(localTimeNoEnd1)!=0) {
                        LocalDateTime tempNoStart1Date = LocalDateTime.of(orderDate,localTimeNoStart1);
                        LocalDateTime tempNoEnd1Date = LocalDateTime.of(orderDate,localTimeNoEnd1);

                        if(localTimeNoStart1.isAfter(localTimeNoEnd1)) {
                            tempNoEnd1Date = LocalDateTime.of(orderDate,localTimeNoEnd1);
                            if(orderWithSla.toLocalTime().isAfter(localTimeNoEnd1) && orderWithSla.toLocalTime().isAfter(localTimeNoStart1)) {
                                tempNoEnd1Date = LocalDateTime.of(orderDate.plusDays(1),localTimeNoEnd1);
                            }
                            else if(orderWithSla.isBefore(tempNoStart1Date) && orderWithSla.isBefore(tempNoEnd1Date) ){
                                tempNoStart1Date = LocalDateTime.of(orderDate.minusDays(1),localTimeNoStart1);
                            }
                        }
                        if((orderWithSla.isEqual(tempNoStart1Date) || orderWithSla.isAfter(tempNoStart1Date)) && orderWithSla.isBefore(tempNoEnd1Date)){
                            tempPromisedDate = tempNoEnd1Date;
                        }
                    }

                    if(localTimeNoStart2!=null && localTimeNoEnd2!=null && localTimeNoStart2.compareTo(localTimeNoEnd2)!=0) {
                        LocalDateTime tempNoStart2Date = LocalDateTime.of(orderDate,localTimeNoStart2);
                        LocalDateTime tempNoEnd2Date = LocalDateTime.of(orderDate,localTimeNoEnd2);

                        if(localTimeNoStart2.isAfter(localTimeNoEnd2)) {
                            if(orderWithSla.toLocalTime().isAfter(localTimeNoEnd2) && orderWithSla.toLocalTime().isAfter(localTimeNoStart2)) {
                                tempNoEnd2Date = LocalDateTime.of(orderDate.plusDays(1),localTimeNoEnd2);
                            }
                            else if(orderWithSla.isBefore(tempNoStart2Date) && orderWithSla.isBefore(tempNoEnd2Date) ){
                                tempNoStart2Date = LocalDateTime.of(orderDate.minusDays(1),localTimeNoStart2);
                            }
                        }

                        if((orderWithSla.isEqual(tempNoStart2Date) || orderWithSla.isAfter(tempNoStart2Date)) && orderWithSla.isBefore(tempNoEnd2Date)){
                            if(tempNoEnd2Date.isAfter(tempPromisedDate)) {
                                tempPromisedDate = tempNoEnd2Date;
                            }
                        }
                    }

                    if(localTimeNoStart3!=null && localTimeNoEnd3!=null && localTimeNoStart3.compareTo(localTimeNoEnd3)!=0) {
                        LocalDateTime tempNoStart3Date = LocalDateTime.of(orderDate,localTimeNoStart3);
                        LocalDateTime tempNoEnd3Date = LocalDateTime.of(orderDate,localTimeNoEnd3);

                        if(localTimeNoStart3.isAfter(localTimeNoEnd3)) {
                            if(orderWithSla.toLocalTime().isAfter(localTimeNoEnd3) && orderWithSla.toLocalTime().isAfter(localTimeNoStart3)) {
                                tempNoEnd3Date = LocalDateTime.of(orderDate.plusDays(1),localTimeNoEnd3);
                            }
                            else if(orderWithSla.isBefore(tempNoStart3Date) && orderWithSla.isBefore(tempNoEnd3Date) ){
                                tempNoStart3Date = LocalDateTime.of(orderDate.minusDays(1),localTimeNoStart3);
                            }
                        }

                        if((orderWithSla.isEqual(tempNoStart3Date) || orderWithSla.isAfter(tempNoStart3Date)) && orderWithSla.isBefore(tempNoEnd3Date)){
                            if(tempNoEnd3Date.isAfter(tempPromisedDate)) {
                                tempPromisedDate = tempNoEnd3Date;
                            }
                        }
                    }
                    if(localTimeNoStart4!=null && localTimeNoEnd4!=null && localTimeNoStart4.compareTo(localTimeNoEnd4)!=0) {
                        LocalDateTime tempNoStart4Date = LocalDateTime.of(orderDate,localTimeNoStart4);
                        LocalDateTime tempNoEnd4Date = LocalDateTime.of(orderDate,localTimeNoEnd4);

                        if(localTimeNoStart4.isAfter(localTimeNoEnd4)) {
                            if(orderWithSla.toLocalTime().isAfter(localTimeNoEnd4) && orderWithSla.toLocalTime().isAfter(localTimeNoStart4)) {
                                tempNoEnd4Date = LocalDateTime.of(orderDate.plusDays(1),localTimeNoEnd4);
                            }
                            else if(orderWithSla.isBefore(tempNoStart4Date) && orderWithSla.isBefore(tempNoEnd4Date) ){
                                tempNoStart4Date = LocalDateTime.of(orderDate.minusDays(1),localTimeNoStart4);
                            }
                        }

                        if((orderWithSla.isEqual(tempNoStart4Date) || orderWithSla.isAfter(tempNoStart4Date)) && orderWithSla.isBefore(tempNoEnd4Date)){
                            if(tempNoEnd4Date.isAfter(tempPromisedDate)) {
                                tempPromisedDate = tempNoEnd4Date;
                            }
                        }
                    }

                    LocalDate tempDate = tempPromisedDate.toLocalDate();
                    LocalDateTime tempDeliveryDate = tempPromisedDate;

                    List<DeliverySlotMasterModel> slotList = deliverySlotMasterDao.findAll();
                    slotList = slotList.stream().sorted(Comparator.comparing(DeliverySlotMasterModel::getSequence)).collect(Collectors.toList());

                    if((isDealerProvidingTruck!=null && isDealerProvidingTruck.equals("true")) || salesOrderDeliverySLA.getCommonTruckCapacity()==null ||salesOrderDeliverySLA.getCommonTruckCapacity()==0) {

                        DeliveryDateAndSlotData data = new DeliveryDateAndSlotData();
                        data.setDeliveryDate(getPossibleDay(tempDeliveryDate, slotList).toString());
                        data.setDeliverySlot(getPossibleSlot(tempDeliveryDate, slotList));
                        data.setOrder(1);
                        data.setQuantity(orderQuantity);
                        data.setMaxTruckPerDay(1000);
                        dataList.add(data);
                        list.setDeliveryDateAndSlots(dataList);
                        return list;
                    }
                    //////////////////////////////////////////////////////////////////////

                    double tempQty = orderQuantity;
                    double truckCapacity =salesOrderDeliverySLA.getCommonTruckCapacity();


                    int sequence = 1;
                    while(diCount>0) {
                        int count = 0;
                        int pendingCount = 0;//orderCountDao.findOrderByExpectedDeliveryDate(user,setSelectedDeliveryDate(tempDate.toString()), routeId);
                        if(pendingCount>0) {
                            if(pendingCount<maxTruckCount) {
                                int diff = maxTruckCount-pendingCount;
                                if(diCount<diff)
                                    count = diCount;
                                else
                                    count = diff;
                                diCount -=diff;
                            }
                        }
                        else {
                            if(diCount<maxTruckCount) {
                                count = diCount;
                            }
                            else {
                                count = maxTruckCount;
                            }
                            diCount -=maxTruckCount;
                        }

                        for(int i=1;i<=count;i++) {
                            DeliveryDateAndSlotData data = new DeliveryDateAndSlotData();
                            data.setDeliveryDate(getPossibleDay(tempDeliveryDate, slotList).toString());
                            data.setDeliverySlot(getPossibleSlot(tempDeliveryDate, slotList));
                            data.setOrder(sequence);
                            data.setTruckCapcity(truckCapacity);
                            if(tempQty>truckCapacity)
                                data.setQuantity(truckCapacity);
                            else
                                data.setQuantity((double)tempQty);
                            tempQty -= truckCapacity;
                            dataList.add(data);
                            sequence++;
                        }
                        tempDate =  tempDate.plusDays(1);
                        tempDeliveryDate = LocalDateTime.of(tempDate,LocalTime.parse("07:00"));

                    }

                    list.setDeliveryDateAndSlots(dataList);
                }
            }
        }
        return list;
    }

    /**
     * @param order
     * @param orderEntry
     * @param status
     */
    @Override
    public void saveOrderRequisitionEntryDetails(OrderModel order, OrderEntryModel orderEntry, String status) {

        if(order.getRequisitions()!=null && !order.getRequisitions().isEmpty()) {
            if(order.getRequisitions().size() == 1) {
                boolean isDeliveredDateNull = false;
                OrderRequisitionModel orderRequisitionModel = order.getRequisitions().get(0);

                OrderRequisitionEntryModel orderRequisitionEntryModel = getModelService().create(OrderRequisitionEntryModel.class);
                orderRequisitionEntryModel.setQuantity(orderEntry.getInvoiceQuantity() * 20);
                orderRequisitionEntryModel.setEntryNumber(orderEntry.getEntryNumber());
                orderRequisitionEntryModel.setEntry(orderEntry);
                orderRequisitionEntryModel.setOrderRequisition(orderRequisitionModel);
                getModelService().save(orderRequisitionEntryModel);

                if(status.equals("EPOD")) {
                    orderRequisitionModel.setReceivedQty(orderRequisitionModel.getReceivedQty() + (orderEntry.getInvoiceQuantity() * 20));
                    if(orderRequisitionModel.getFulfilledDate()==null) {
                        orderRequisitionModel.setStatus(RequisitionStatus.PENDING_DELIVERY);
                        orderRequisitionModel.setFulfilledDate(new Date());
                    }

                    for(AbstractOrderEntryModel entry : order.getEntries()) {
                        if(entry.getDeliveredDate() == null) {
                            isDeliveredDateNull = true;
                            break;
                        }
                    }
                    if(!isDeliveredDateNull) {
                        orderRequisitionModel.setStatus(RequisitionStatus.DELIVERED);
                        orderRequisitionModel.setDeliveredDate(new Date());
                        orderRequisitionService.orderCountIncrementForDealerRetailerMap(orderRequisitionModel.getDeliveredDate(),orderRequisitionModel.getFromCustomer(), orderRequisitionModel.getToCustomer(), baseSiteService.getCurrentBaseSite());

                        NuvocoCustomerModel currentUser = (NuvocoCustomerModel) userService.getCurrentUser();
                        LOGGER.info("1. Retailer RECEIPT::: In DefaultSCLB2BOrderService:: Record found--- Requisition Status... " + orderRequisitionModel.getStatus()
                                + " Current customer No -->" + currentUser.getCustomerNo());
                        if (null != orderRequisitionModel.getFromCustomer() && null != currentUser) {
                            updateRetailerReceipts(orderRequisitionModel.getProduct(), orderRequisitionModel.getFromCustomer(), orderRequisitionModel.getReceivedQty());
                        }
                    }
                    else {
                        getRequisitionStatusByOrderLines(order);
                    }

                }

                getModelService().save(orderRequisitionModel);

            }
        }

    }


    private void updateRetailerReceipts(ProductModel productCode, NuvocoCustomerModel dealerCode, Double receivedQuantity) {
        RetailerRecAllocateModel receiptRetailerAllocate = dealerDao.getRetailerAllocation(productCode, dealerCode);
        if (null != receiptRetailerAllocate) {
            LOGGER.info("1. Retailer RECEIPT:::DefaultSCLB2BOrderService:: Record found--- Receipts for Dealer " + receiptRetailerAllocate.getReceipt()
                    + " Dealer No -->" + receiptRetailerAllocate.getDealerCode()
                   /* + " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
                    + " Available allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer()*/);
           // receiptRetailerAllocate.setSalesToInfluencer((new Double(receivedQuantity)).intValue());
           // int stockRetailerToInfluencer = (int) ((1.0 * (receiptRetailerAllocate.getReceipt()	- receiptRetailerAllocate.getSalesToInfluencer())));
            //receiptRetailerAllocate.setStockAvlForInfluencer(stockRetailerToInfluencer);
            LOGGER.info("2. Retailer RECEIPT:::DefaultSCLB2BOrderService:: Updated " + receiptRetailerAllocate.getReceipt()
                /*    + " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
                    + " Allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer()*/);
            getModelService().save(receiptRetailerAllocate);
        } else {
            //If product and dealer is not found in the RetailerRecAllocate
            //then it means new entry has to be made as orderrequisition is placed with this combination
            RetailerRecAllocateModel receiptRetailerAllocateNew = modelService.create(RetailerRecAllocateModel.class);
            receiptRetailerAllocateNew.setProduct(productCode.getPk().toString());
            receiptRetailerAllocateNew.setDealerCode(dealerCode.getPk().toString());
            Double updatedQty = receivedQuantity;
            receiptRetailerAllocateNew.setReceipt((null != updatedQty)?updatedQty.intValue():0);
            receiptRetailerAllocateNew.setSalesToInfluencer(0);
          //  int stockRetailerInfluencer = (int) ((1.0 * (receiptRetailerAllocateNew.getReceipt() - receiptRetailerAllocateNew.getSalesToInfluencer())));
           // receiptRetailerAllocateNew.setStockAvlForInfluencer(stockRetailerInfluencer);
            getModelService().save(receiptRetailerAllocateNew);
            getModelService().refresh(receiptRetailerAllocateNew);
        }
    }

    /**
     * @return
     */
    @Override
    public List<DeliverySlotMasterModel> getDeliverySlotList() {
        return deliverySlotMasterDao.findAll();
    }


    private int getDICountForOrder(final double truckCapacity , double orderQty) {
        if(truckCapacity>0) {
            return (int) Math.ceil(orderQty/(double)truckCapacity);
        }
        return 0;
    }

    private LocalDateTime getPossibleDay(LocalDateTime tempDeliveryDate , List<DeliverySlotMasterModel> list) {
        LocalDateTime possibleDate = tempDeliveryDate;
        if(list!=null && !list.isEmpty()) {
            DeliverySlotMasterModel lastSlot= list.get(list.size()-1);
            LocalDateTime endTime = LocalDateTime.of(tempDeliveryDate.toLocalDate(),LocalTime.parse(lastSlot.getEnd()));
            if(tempDeliveryDate.isEqual(endTime) || tempDeliveryDate.isAfter(endTime)) {
                DeliverySlotMasterModel firstSlot= list.get(0);
                possibleDate = LocalDateTime.of(tempDeliveryDate.plusDays(1).toLocalDate(),LocalTime.parse(firstSlot.getStart()));
            }
        }
        return possibleDate;
    }

    protected Set<OrderStatus> extractOrderStatuses(final String statuses)
    {
        final String[] statusesStrings = statuses.split(NuvocoCoreConstants.ORDER.ENUM_VALUES_SEPARATOR);

        final Set<OrderStatus> statusesEnum = new HashSet<>();
        for (final String status : statusesStrings)
        {
            statusesEnum.add(OrderStatus.valueOf(status));
        }
        return statusesEnum;
    }


    private String getPossibleSlot(LocalDateTime tempDeliveryDate, List<DeliverySlotMasterModel> list) {
        String morningSlot = DeliverySlots.SEVENTOELEVEN.getCode();
        for(DeliverySlotMasterModel slot: list) {
            LocalTime startTime = LocalTime.parse(slot.getStart()).minusSeconds(1);
            LocalTime endTime = LocalTime.parse(slot.getEnd());
            LocalTime deliveryTime = tempDeliveryDate.toLocalTime();
            if(deliveryTime.isAfter(startTime) && deliveryTime.isBefore(endTime)) {
                morningSlot = slot.getSlot().getCode();
            }
        }
        return morningSlot;
    }

}
