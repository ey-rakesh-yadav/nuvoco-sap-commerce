package com.nuvoco.core.services.impl;

import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.dao.NuvocoUserDao;
import com.nuvoco.core.dao.OrderRequisitionDao;
import com.nuvoco.core.enums.DeliverySlots;
import com.nuvoco.core.enums.NetworkType;
import com.nuvoco.core.enums.RequisitionStatus;
import com.nuvoco.core.enums.ServiceType;
import com.nuvoco.core.model.*;
import com.nuvoco.core.services.OrderRequisitionService;
import com.nuvoco.core.services.TerritoryManagementService;
import com.nuvoco.facades.data.OrderRequisitionData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class OrderRequisitionServiceImpl implements OrderRequisitionService {


    private static final Logger LOGGER = Logger.getLogger(OrderRequisitionServiceImpl.class);

    @Autowired
    OrderRequisitionDao orderRequisitionDao;


    @Autowired
    UserService userService;

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    TerritoryManagementService territoryManagementService;

    @Autowired
    FlexibleSearchService flexibleSearchService;

    @Autowired
    ModelService modelService;

    @Resource
    KeyGenerator orderRequisitionIdGenerator;

    @Autowired
    CatalogVersionService catalogVersionService;

    @Autowired
    ProductService productService;

    @Autowired
    NuvocoUserDao nuvocoUserDao;

    /**
     * @param orderRequisitionData
     * @return
     */
    @Override
    public boolean saveOrderRequisitionDetails(OrderRequisitionData orderRequisitionData) {
        OrderRequisitionModel orderRequisitionModel;
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        NuvocoCustomerModel retailer;
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        if (orderRequisitionData.getRequisitionId() != null && !orderRequisitionData.getRequisitionId().isEmpty()) {
            orderRequisitionModel = orderRequisitionDao.findByRequisitionId(orderRequisitionData.getRequisitionId());
        } else {
            orderRequisitionModel = modelService.create(OrderRequisitionModel.class);
            orderRequisitionModel.setRequisitionId(orderRequisitionIdGenerator.generate().toString());
        }

        if (orderRequisitionData.getIsDraft()) {
            orderRequisitionModel.setIsRequisitionPlaced(false);
        } else {
            orderRequisitionModel.setIsRequisitionPlaced(true);
            orderRequisitionModel.setStatus(RequisitionStatus.valueOf("PENDING_CONFIRMATION"));
        }

        if(orderRequisitionData.getOrderCode()!=null && !orderRequisitionData.getOrderCode().isEmpty()) {
            if(orderRequisitionDao.findOrderByCode(orderRequisitionData.getOrderCode())!=null) {
                OrderModel orderModel = orderRequisitionDao.findOrderByCode(orderRequisitionData.getOrderCode());
                orderRequisitionModel.setOrder(orderModel);
                orderRequisitionModel.setStatus(RequisitionStatus.PENDING_FULFILLMENT);
                orderRequisitionModel.setServiceType(ServiceType.PLACED);
                if(orderModel.getOrderAcceptedDate()!=null) {
                    orderRequisitionModel.setRequisitionDate(orderModel.getOrderAcceptedDate());
                    orderRequisitionModel.setAcceptedDate(orderModel.getOrderAcceptedDate());
                }
            }

        }
        else {
            orderRequisitionModel.setRequisitionDate(new Date());
        }

        if (orderRequisitionData.getProductCode() != null && !(orderRequisitionData.getProductCode().isEmpty())) {

//            if(orderRequisitionData.getBaseSiteUid()!=null && !orderRequisitionData.getBaseSiteUid().isEmpty()) {
//                CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(orderRequisitionData.getBaseSiteUid() + "ProductCatalog", "Online");
//                ProductModel product = productService.getProductForCode(catalogVersion, orderRequisitionData.getProductCode());
//
//                orderRequisitionModel.setProduct(product);
//            }

            if(baseSiteService.getCurrentBaseSite()!=null) {
                CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                ProductModel product = productService.getProductForCode(catalogVersion, orderRequisitionData.getProductCode());

                orderRequisitionModel.setProduct(product);
            }


        }

        if (orderRequisitionData.getFromCustomerUid() != null && !(orderRequisitionData.getFromCustomerUid().isEmpty())) {
            NuvocoCustomerModel dealer = (NuvocoCustomerModel) userService.getUserForUID(orderRequisitionData.getFromCustomerUid());
            if (!Objects.isNull(dealer)) {
                orderRequisitionModel.setFromCustomer(dealer);
            }
        }

        if (orderRequisitionData.getToCustomerUid()!=null && !orderRequisitionData.getToCustomerUid().isEmpty()) {
            retailer = (NuvocoCustomerModel) userService.getUserForUID(orderRequisitionData.getToCustomerUid());
        }
        else {
            retailer = (NuvocoCustomerModel) userService.getCurrentUser();
        }
        if(!Objects.isNull(retailer)) {
            orderRequisitionModel.setToCustomer(retailer);
        }



        if (!Objects.isNull(orderRequisitionData.getDeliveryAddress())) {
            if (orderRequisitionData.getDeliveryAddress().getId()!=null && !orderRequisitionData.getDeliveryAddress().getId().isEmpty() && !Objects.isNull(nuvocoUserDao.getAddressByPk(orderRequisitionData.getDeliveryAddress().getId()))) {
//                orderRequisitionModel.setDeliveryAddress(sclUserDao.getAddressByErpId(orderRequisitionData.getDeliveryAddress().getErpId(),retailer));
                orderRequisitionModel.setDeliveryAddress(nuvocoUserDao.getAddressByPk(orderRequisitionData.getDeliveryAddress().getId()));
            }
        }

        orderRequisitionModel.setQuantity(orderRequisitionData.getQuantity());
        orderRequisitionModel.setReceivedQty(0.0);

        if (orderRequisitionData.getExpectedDeliverySlot() != null && !(orderRequisitionData.getExpectedDeliverySlot().isEmpty())) {
            if (!Objects.isNull(DeliverySlots.valueOf(orderRequisitionData.getExpectedDeliverySlot()))) {
                orderRequisitionModel.setExpectedDeliverySlot(DeliverySlots.valueOf(orderRequisitionData.getExpectedDeliverySlot()));
            }
        }

        if (orderRequisitionData.getExpectedDeliveryDate() != null && !(orderRequisitionData.getExpectedDeliveryDate().isEmpty())) {
            try {
                orderRequisitionModel.setExpectedDeliveryDate(dateFormat.parse(orderRequisitionData.getExpectedDeliveryDate()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        orderRequisitionModel.setActive(true);
        //orderRequisitionModel.setStatus(RequisitionStatus.valueOf("PENDING_CONFIRMATION"));

        if(Objects.nonNull(orderRequisitionData.getToCustomerUid()) && territoryManagementService.getTerritoriesForCustomer(orderRequisitionData.getToCustomerUid()) != null && !(territoryManagementService.getTerritoriesForCustomer(orderRequisitionData.getToCustomerUid()).isEmpty())){

            SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoriesForCustomer(orderRequisitionData.getToCustomerUid()).get(0);
            if(subAreaMaster != null) {
                orderRequisitionModel.setSubAreaMaster(subAreaMaster);
                DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                if(district!=null) {
                    orderRequisitionModel.setDistrictMaster(district);
                    RegionMasterModel region = district.getRegion();
                    if(region!=null) {
                        orderRequisitionModel.setRegionMaster(region);
                    }
                }

            }
        }

        orderRequisitionModel.setSaleSummaryJobStatus("N");
        modelService.save(orderRequisitionModel);

        //To update the quantity as sales to retailer for allocation calculation **
        if (null != orderRequisitionModel
                && RequisitionStatus.PENDING_CONFIRMATION.equals(orderRequisitionModel.getStatus())
                && null != orderRequisitionModel.getProduct() && null != orderRequisitionModel.getFromCustomer()) {
            LOGGER.info("1. RECEIPT::: In Save order Requisition called... "
                    + " Upodate stock quantity sales to retailer allocated--->"
                    + "when RequisitionStatus.PENDING_FULFILLMENT--> and requistion quantity is--->>"
                    + orderRequisitionModel.getQuantity());
            LOGGER.info("2. RECEIPT::: Product PK from product model passed -->" + orderRequisitionModel.getProduct().getPk().toString());
            LOGGER.info("3. RECEIPT::: Dealer PK from dealer model passed -->" + orderRequisitionModel.getFromCustomer().getPk().toString());
            ReceiptAllocaltionModel receiptAllocate = getDealerAllocation(orderRequisitionModel.getProduct(),
                    orderRequisitionModel.getFromCustomer());

            Double updatedQty = 0.0;
            int stockAvailableForRetailer = 0;
            int stockAvailableForInfluencer = 0;
            if (null != receiptAllocate) {
                LOGGER.info("4. RECEIPT:::Record found--- Requisition... "
                        + " Available receipt stock-->" + receiptAllocate.getReceipt()
                        + " Available stock for retailer -->" + receiptAllocate.getStockAvlForRetailer()
                       /* + " Available stock for influencer -->" + receiptAllocate.getStockAvlForInfluencer()*/);
                updatedQty = receiptAllocate.getSalesToRetailer() + orderRequisitionModel.getQuantity();
                receiptAllocate.setSalesToRetailer(updatedQty.intValue());
                stockAvailableForRetailer = getStockAvailForRetailer(receiptAllocate.getReceipt(),
                        updatedQty.intValue(), receiptAllocate.getSalesToInfluencer());
              /*  stockAvailableForInfluencer = getStockAvailForInfluencer(receiptAllocate.getReceipt(),
                        receiptAllocate.getSalesToRetailer(), receiptAllocate.getSalesToInfluencer());
                receiptAllocate.setStockAvlForInfluencer(stockAvailableForInfluencer);*/
                receiptAllocate.setStockAvlForRetailer(stockAvailableForRetailer);
                modelService.save(receiptAllocate);
                LOGGER.info("5. RECEIPT::: Record found--- After updating--"
                        + " Available Receipt stock-->" + receiptAllocate.getReceipt()
                        + " Available stock for retailer (increase) -->" + receiptAllocate.getStockAvlForRetailer()
                     /*   + " Available stock for influencer -->" + receiptAllocate.getStockAvlForInfluencer()*/);
            } else {
                LOGGER.info("6. RECEIPT::: Stock allocation --> "
                        + " New entry---OrderRequisition for Product or Dealer not found");
                updatedQty = orderRequisitionModel.getQuantity() * NuvocoCoreConstants.QUANTITY_INMT_TO_BAGS;
                //If product and dealer is not found in the RetailerRecAllocate
                //then it means new entry has to be made as orderrequisition is placed with this combination
                ReceiptAllocaltionModel receiptRetailerAllocateNew = modelService.create(ReceiptAllocaltionModel.class);
                receiptRetailerAllocateNew.setProduct(orderRequisitionModel.getProduct().getPk().toString());
                receiptRetailerAllocateNew.setDealerCode(orderRequisitionModel.getFromCustomer().getPk().toString());
                receiptRetailerAllocateNew.setReceipt((null != updatedQty)?updatedQty.intValue():0);
                receiptRetailerAllocateNew.setSalesToInfluencer(0);
                int stockRetailerInfluencer = (int) ((1.0 * (receiptRetailerAllocateNew.getReceipt() - receiptRetailerAllocateNew.getSalesToInfluencer())));
                receiptRetailerAllocateNew.setStockAvlForInfluencer(stockRetailerInfluencer);
                modelService.save(receiptRetailerAllocateNew);
                modelService.refresh(receiptRetailerAllocateNew);
            }
        }

        if(!orderRequisitionData.getIsDraft()) {
            saveDealerRetailerMapping((NuvocoCustomerModel) userService.getUserForUID(orderRequisitionData.getFromCustomerUid()), retailer, brand);
        }
        return true;
    }


    //To get the stock available for Retailer
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
     * @param statuses
     * @param submitType
     * @param fromMonth
     * @param fromYear
     * @param toMonth
     * @param toYear
     * @param currentUser
     * @param productCode
     * @param searchPageData
     * @param requisitionId
     * @param searchKey
     * @return
     */
    @Override
    public SearchPageData<OrderRequisitionModel> getOrderRequisitionDetails(List<String> statuses, String submitType, Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, NuvocoCustomerModel currentUser, String productCode, SearchPageData searchPageData, String requisitionId, String searchKey) {
        String fromDate = null;
        String toDate = null;
        if(fromMonth!=null && fromMonth!=0) {
            int fYear = Integer.parseInt(fromYear.toString());
            int fMonth = Integer.parseInt(fromMonth.toString());
            int tYear = Integer.parseInt(toYear.toString());
            int tMonth = Integer.parseInt(toMonth.toString());

            if(submitType.equals("Draft") || submitType.equals("draft")) {
                if(fMonth >= 1 && fMonth <=12) {
                    if(fMonth == 12) {
                        tMonth = 1;
                        tYear = fYear + 1;
                    }
                    else if(fMonth<12) {
                        tMonth = fMonth + 1;
                    }
                }
            }

            String singleDigitFromMonth = Integer.toString(fYear)+"-0"+Integer.toString(fMonth)+"-"+"01";
            String singleDigitToMonth =  Integer.toString(tYear)+"-0"+Integer.toString(tMonth)+"-"+"01";
            String doubleDigitFromMonth = Integer.toString(fYear)+"-"+Integer.toString(fMonth)+"-"+"01";
            String doubleDigitToMonth = Integer.toString(tYear)+"-"+Integer.toString(tMonth)+"-"+"01";

            fromDate = (fMonth>=1 && fMonth<=9) ? singleDigitFromMonth : doubleDigitFromMonth;
            toDate = (tMonth>=1 && tMonth<=9) ? singleDigitToMonth : doubleDigitToMonth;
        }
        return orderRequisitionDao.getOrderRequisitionDetails(statuses, submitType, fromDate, toDate, currentUser, productCode, searchPageData, requisitionId, searchKey);
    }

    /**
     * @param requisitionId
     * @param status
     * @param receivedQty
     * @param cancelReason
     * @return
     */
    @Override
    public Boolean updateOrderRequistionStatus(String requisitionId, String status, Double receivedQty, String cancelReason) {
        OrderRequisitionModel model = orderRequisitionDao.findByRequisitionId(requisitionId);
        if(status!=null && status.equals("DELIVERED")){
            if(model.getStatus()!=null && model.getStatus().equals(RequisitionStatus.PENDING_DELIVERY)){
                Date deliveryDate = new Date();

                model.setStatus(RequisitionStatus.DELIVERED);
                model.setDeliveredDate(deliveryDate);
                model.setReceivedQty(receivedQty);

                modelService.save(model);

                NuvocoCustomerModel currentUser = (NuvocoCustomerModel) userService.getCurrentUser();
                LOGGER.info("1. Retailer RECEIPT:::Record found--- Requisition Status... " + model.getStatus()
                        + " Current customer No -->" + currentUser.getCustomerNo());
                if (null != model.getFromCustomer() && null != currentUser) {
                    updateRetailerReceipts(model.getProduct(), model.getToCustomer(), receivedQty);
                }
            }
            else{
                throw new UnsupportedOperationException();
            }
        }
        else if(status!=null && status.equals("CANCELLED")){
            if(model.getStatus()!=null && model.getStatus().equals(RequisitionStatus.PENDING_CONFIRMATION)){
                Date cancelledDate = new Date();
                NuvocoCustomerModel currentUser = (NuvocoCustomerModel) userService.getCurrentUser();

                model.setStatus(RequisitionStatus.CANCELLED);
                model.setCancelledDate(cancelledDate);
                model.setCancelReason(cancelReason);
                model.setCancelledBy(currentUser);

                modelService.save(model);
            }
            else{
                throw new UnsupportedOperationException();
            }
        }
        else if(status!=null && status.equals("DELETE")){
            model.setActive(false);
            modelService.save(model);
        }
        else{
            throw new UnsupportedOperationException();
        }
        return true;
    }

    //To update the quantity as receipts for allocation calculation
    private void updateRetailerReceipts(ProductModel productCode, NuvocoCustomerModel dealerCode, Double receivedQuantity) {
        RetailerRecAllocateModel receiptRetailerAllocate = getRetailerAllocation(productCode, dealerCode);
        if (null != receiptRetailerAllocate) {
            LOGGER.info("1. Retailer RECEIPT:::Record found--- Receipts for Dealer " + receiptRetailerAllocate.getReceipt()
                    + " Dealer No -->" + receiptRetailerAllocate.getDealerCode()
                    + " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
                    + " Available allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer());
//  			Double updatedQty = receiptRetailerAllocate.getReceipt() - (receivedQuantity * SclCoreConstants.QUANTITY_INMT_TO_BAGS);
            //receiptRetailerAllocate.setReceipt((null != updatedQty)?updatedQty.intValue():0);
            receiptRetailerAllocate.setSalesToInfluencer((new Double(receivedQuantity)).intValue());
            int stockRetailerToInfluencer = (int) ((1.0 * (receiptRetailerAllocate.getReceipt()	- receiptRetailerAllocate.getSalesToInfluencer())));
            receiptRetailerAllocate.setStockAvlForInfluencer(stockRetailerToInfluencer);
            LOGGER.info("2. Retailer RECEIPT:::Updated " + receiptRetailerAllocate.getReceipt()
                    + " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
                    + " Allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer());
            modelService.save(receiptRetailerAllocate);
        } else {
            //If product and dealer is not found in the RetailerRecAllocate
            //then it means new entry has to be made as orderrequisition is placed with this combination
            RetailerRecAllocateModel receiptRetailerAllocateNew = modelService.create(RetailerRecAllocateModel.class);
            receiptRetailerAllocateNew.setProduct(productCode.getPk().toString());
            receiptRetailerAllocateNew.setDealerCode(dealerCode.getPk().toString());
            Double updatedQty = receivedQuantity;
            receiptRetailerAllocateNew.setReceipt((null != updatedQty)?updatedQty.intValue():0);
            receiptRetailerAllocateNew.setSalesToInfluencer(0);
            int stockRetailerInfluencer = (int) ((1.0 * (receiptRetailerAllocateNew.getReceipt() - receiptRetailerAllocateNew.getSalesToInfluencer())));
            receiptRetailerAllocateNew.setStockAvlForInfluencer(stockRetailerInfluencer);
            modelService.save(receiptRetailerAllocateNew);
            modelService.refresh(receiptRetailerAllocateNew);
        }
    }

    /**
     * @param dealer
     * @param retailer
     * @param brand
     */
    @Override
    public void saveDealerRetailerMapping(NuvocoCustomerModel dealer, NuvocoCustomerModel retailer, BaseSiteModel brand) {
        DealerRetailerMapModel dealerRetailerMapModel = orderRequisitionDao.getDealerforRetailerDetails(dealer, retailer, brand);
        if(Objects.isNull(dealerRetailerMapModel)) {
            dealerRetailerMapModel = modelService.create(DealerRetailerMapModel.class);
            dealerRetailerMapModel.setDealer(dealer);
            dealerRetailerMapModel.setRetailer(retailer);
            dealerRetailerMapModel.setBrand(brand);
            dealerRetailerMapModel.setActive(true);
            modelService.save(dealerRetailerMapModel);
        }
        else if(!dealerRetailerMapModel.getActive()) {
            dealerRetailerMapModel.setActive(true);
            modelService.save(dealerRetailerMapModel);
        }

    }

    /**
     * @param deliveredDate
     * @param dealer
     * @param retailer
     * @param brand
     */
    @Override
    public void orderCountIncrementForDealerRetailerMap(Date deliveredDate, NuvocoCustomerModel dealer, NuvocoCustomerModel retailer, BaseSiteModel brand) {

        DealerRetailerMapModel dealerRetailerMapModel = orderRequisitionDao.getDealerforRetailerDetails(dealer, retailer, brand);
        if(!Objects.isNull(dealerRetailerMapModel)) {
            dealerRetailerMapModel.setOrderCount(dealerRetailerMapModel.getOrderCount()+1);
            NuvocoCustomerModel retailerModel = dealerRetailerMapModel.getRetailer();
            retailerModel.setLastLiftingDate(deliveredDate);
            retailerModel.setNetworkType(NetworkType.ACTIVE.getCode());
            modelService.save(retailerModel);
            dealerRetailerMapModel.setLastLiftingDate(deliveredDate);
            modelService.save(dealerRetailerMapModel);
        }
    }

    protected ReceiptAllocaltionModel getDealerAllocation(ProductModel productCode, NuvocoCustomerModel dealerCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {ReceiptAllocaltion} WHERE {dealerCode}=?dealerCode AND {product}=?product");

        params.put("dealerCode", dealerCode);
        params.put("product", productCode);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(ReceiptAllocaltionModel.class));
        query.addQueryParameters(params);
        final SearchResult<ReceiptAllocaltionModel> searchResult = flexibleSearchService.search(query);
        LOGGER.info("In DealerDao:getDealerAllocation method--> query:::" + builder.toString() + ":::Product Code:::" + productCode.getPk().toString() + ":::Dealer Code:::" + dealerCode.getPk().toString());
        LOGGER.info("In DealerDao:getDealerAllocation method--> Show the result of the query:::" + searchResult.getResult());
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    protected RetailerRecAllocateModel getRetailerAllocation(ProductModel productCode, NuvocoCustomerModel dealerCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {RetailerRecAllocate} WHERE {dealerCode}=?dealerCode AND {product}=?product");

        params.put("dealerCode", dealerCode);
        params.put("product", productCode);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(RetailerRecAllocateModel.class));
        query.addQueryParameters(params);
        final SearchResult<RetailerRecAllocateModel> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

}
