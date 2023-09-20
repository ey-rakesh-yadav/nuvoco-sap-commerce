package com.nuvoco.facades.populators;

import com.nuvoco.core.enums.IncoTerms;
import com.nuvoco.facades.data.TrackingData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class NuvocoTrackingDetailsOnEntryPopulator implements Populator<OrderEntryModel, List<TrackingData>> {

    @Autowired
    private EnumerationService enumerationService;


    @Override
    public void populate(OrderEntryModel source, List<TrackingData> target) throws ConversionException {

        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");

        TrackingData truckAllocatedTrackingData = new TrackingData();
        truckAllocatedTrackingData.setCode(OrderStatus.TRUCK_ALLOCATED.getCode());
        truckAllocatedTrackingData.setName(enumerationService.getEnumerationName(OrderStatus.TRUCK_ALLOCATED));
        truckAllocatedTrackingData.setActualTime(source.getTruckAllocatedDate());
        truckAllocatedTrackingData.setIndex(3);

        List<String> comments = new ArrayList<String>();
        if(source.getTruckAllocatedQty()!=null && source.getTruckAllocatedQty()>0)
            comments.add("Truck Allocated Quantity : " + source.getTruckAllocatedQty());
        if(source.getInvoiceCreationDateAndTime()!=null) {
            if(source.getDeliveryQty()!=null && source.getDeliveryQty()>0)
                comments.add("Delivery Quantity : " + source.getDeliveryQty());
            if(StringUtils.isNotBlank(source.getErpTruckNumber()))
                comments.add("Vehicle number : " + source.getErpTruckNumber());
            if(StringUtils.isNotBlank(source.getErpDriverNumber()))
                comments.add("Driver contact Number : " + source.getErpDriverNumber());
            if(StringUtils.isNotBlank(source.getTransporterName()))
                comments.add("Transporter Name : " + source.getTransporterName());
            if(StringUtils.isNotBlank(source.getTransporterPhoneNumber()))
                comments.add("Transporter Number : " + source.getTransporterPhoneNumber());
        }
        truckAllocatedTrackingData.setComment(comments);

        target.add(truckAllocatedTrackingData);

        TrackingData truckDispatchedTrackingData = new TrackingData();
        truckDispatchedTrackingData.setCode(OrderStatus.TRUCK_DISPATCHED.getCode());
        truckDispatchedTrackingData.setName(enumerationService.getEnumerationName(OrderStatus.TRUCK_DISPATCHED));
        truckDispatchedTrackingData.setActualTime(source.getTruckDispatcheddate());
        truckDispatchedTrackingData.setIndex(4);
        target.add(truckDispatchedTrackingData);

        if(source.getFob()!=null && source.getFob().equals(IncoTerms.FOR)) {
            TrackingData truckReachedTrackingData = new TrackingData();
            truckReachedTrackingData.setCode(OrderStatus.TRUCK_REACHED_DESTINATION.getCode());
            truckReachedTrackingData.setName(enumerationService.getEnumerationName(OrderStatus.TRUCK_REACHED_DESTINATION));
            truckReachedTrackingData.setActualTime(source.getTruckReachedDate());
            truckReachedTrackingData.setIndex(5);

            if(source.getEpodInitiateDate()!=null) {
                List<String> epodComments = new ArrayList<String>();
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                String strDate = dateFormat.format(source.getEpodInitiateDate());
                epodComments.add("Truck arrival confirmed time : " + strDate);
                truckReachedTrackingData.setComment(epodComments);
            }
            target.add(truckReachedTrackingData);
        }

        TrackingData deliveredTrackingData = new TrackingData();
        deliveredTrackingData.setCode(OrderStatus.DELIVERED.getCode());
        deliveredTrackingData.setName(enumerationService.getEnumerationName(OrderStatus.DELIVERED));
        deliveredTrackingData.setActualTime(source.getDeliveredDate());
        deliveredTrackingData.setIndex(6);
        target.add(deliveredTrackingData);

        TrackingData cancelledTrackingData = new TrackingData();
        cancelledTrackingData.setCode(OrderStatus.CANCELLED.getCode());
        cancelledTrackingData.setName(enumerationService.getEnumerationName(OrderStatus.CANCELLED));
        cancelledTrackingData.setActualTime(source.getCancelledDate());
        cancelledTrackingData.setIndex(7);
        target.add(cancelledTrackingData);

    }
}
