package com.nuvoco.core.strategies.impl;

import com.nuvoco.core.enums.DeliverySlots;
import com.nuvoco.core.services.NuvocoB2BOrderService;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceAddToCartStrategy;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DefaultNuvocoModifyOrderStrategy extends DefaultCommerceAddToCartStrategy {

    private static final Logger LOGGER = Logger.getLogger(DefaultNuvocoModifyOrderStrategy.class);

    @Resource
    private NuvocoB2BOrderService b2bOrderService;

    @Autowired
    WarehouseService warehouseService;


    public void modifyOrderEntry(final OrderModel orderModel , final CommerceCartParameter parameter ) throws CommerceCartModificationException {

        final UnitModel unit = getUnit(parameter);
        final long actualAllowedQuantityChange = getAllowedOrderAdjustmentForProduct(parameter.getProduct(),parameter.getQuantity() ,parameter.getPointOfService());
        final AbstractOrderEntryModel abstractOrderEntryModel = b2bOrderService.addNewOrderEntry(orderModel,parameter.getProduct(),(int)actualAllowedQuantityChange,unit,(int)parameter.getEntryNumber());
        abstractOrderEntryModel.setTruckNo(parameter.getTruckNo());
        abstractOrderEntryModel.setDriverContactNo(parameter.getDriverContactNo());
        if(parameter.getSelectedDeliverySlot()!=null) {
            abstractOrderEntryModel.setExpectedDeliveryslot(DeliverySlots.valueOf(parameter.getSelectedDeliverySlot()));
        }
        if(parameter.getSelectedDeliveryDate()!=null) {
            abstractOrderEntryModel.setExpectedDeliveryDate(setSelectedDeliveryDate(parameter.getSelectedDeliveryDate()));
        }
        if(parameter.getCalculatedDeliveryDate()!=null) {
            abstractOrderEntryModel.setCalculatedDeliveryDate(setSelectedDeliveryDate(parameter.getCalculatedDeliveryDate()));
        }
        if(parameter.getCalculatedDeliverySlot()!=null) {
            abstractOrderEntryModel.setCalculatedDeliveryslot(DeliverySlots.valueOf(parameter.getCalculatedDeliverySlot()));
        }
        if(parameter.getQuantityMT()!=null) {
            abstractOrderEntryModel.setQuantityInMT(parameter.getQuantityMT());
        }
        abstractOrderEntryModel.setSequence(parameter.getSequence());

        if(parameter.getWarehouseCode()!=null) {
            WarehouseModel warehouse =  warehouseService.getWarehouseForCode(parameter.getWarehouseCode());
            abstractOrderEntryModel.setSource(warehouse);

            /*List<FreightAndIncoTermsMasterModel> freightAndIncoTerms = slctCrmIntegrationService.findFreightAndIncoTerms(orderModel.getDeliveryAddress().getState(), orderModel.getDeliveryAddress().getDistrict(),
                    orderModel.getSite(), abstractOrderEntryModel.getSource().getType().getCode());
            if(freightAndIncoTerms != null && !freightAndIncoTerms.isEmpty()) {
                for (FreightAndIncoTermsMasterModel f : freightAndIncoTerms) {
                    abstractOrderEntryModel.setFob(IncoTerms.valueOf(f.getIncoTerms()));
                    abstractOrderEntryModel.setFreightTerms(FreightTerms.valueOf(f.getFrieghtTerms()));
                    if(abstractOrderEntryModel.getFob()!=null && abstractOrderEntryModel.getFob().equals(IncoTerms.EX))
                        abstractOrderEntryModel.setEpodCompleted(true);
                    break;
                }
            }*/

        }
        abstractOrderEntryModel.setRouteId(parameter.getRouteId());
        abstractOrderEntryModel.setRemarks(parameter.getRemarks());
        getModelService().save(abstractOrderEntryModel);
//        OrderProcessModel orderProcessModel;
//        Optional<OrderProcessModel> orderProcess = orderModel.getOrderProcess().stream().filter(op -> op instanceof OrderProcessModel).findFirst();
//        if(orderProcess.isPresent()){
//            orderProcessModel = orderProcess.get();
//        }
//        else{
//            throw new ModelNotFoundException("No Order Process Found for Order: "+orderModel.getCode());
//        }
//        businessProcessService.triggerEvent(orderProcessModel.getCode()+"_"+ SclCoreConstants.APPROVAL_CONSTANT.ORDER_REVIEW_DECISION_EVENT_NAME);
    }


    protected long getAllowedOrderAdjustmentForProduct(final ProductModel productModel,
                                                       final long quantityToAdd, final PointOfServiceModel pointOfServiceModel)
    {

        final long stockLevel = getAvailableStockLevel(productModel, pointOfServiceModel);

        // How many will we have in our cart if we add quantity
        //final long newTotalQuantity = quantityToAdd;

        // Now limit that to the total available in stock
        final long newTotalQuantityAfterStockLimit = Math.min(quantityToAdd, stockLevel);

        // So now work out what the maximum allowed to be added is (note that
        // this may be negative!)
        final Integer maxOrderQuantity = productModel.getMaxOrderQuantity();

        if (isMaxOrderQuantitySet(maxOrderQuantity))
        {
            return Math.min(newTotalQuantityAfterStockLimit, maxOrderQuantity.longValue());
        }
        return newTotalQuantityAfterStockLimit ;
    }

    private Date setSelectedDeliveryDate(String selectedDeliveryDate) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(selectedDeliveryDate);
        } catch (ParseException e) {
            LOGGER.error("Error Parsing Selected Delivery Date", e);
            throw new IllegalArgumentException(String.format("Please provide valid date %s", selectedDeliveryDate));
        }
        return date;
    }

}
