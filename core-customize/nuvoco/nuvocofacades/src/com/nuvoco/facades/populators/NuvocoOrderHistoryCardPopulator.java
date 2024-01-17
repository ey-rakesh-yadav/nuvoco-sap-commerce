package com.nuvoco.facades.populators;

import com.nuvoco.facades.data.NuvocoOrderHistoryData;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.stream.Collectors;

public class NuvocoOrderHistoryCardPopulator implements Populator<AbstractOrderModel, NuvocoOrderHistoryData> {

    @Autowired
    Converter<AddressModel, AddressData> addressConverter;

    @Autowired
    private PriceDataFactory priceDataFactory;

    @Autowired
    Converter<MediaModel, ImageData> imageConverter;

    @Override
    public void populate(AbstractOrderModel source, NuvocoOrderHistoryData target) throws ConversionException {

        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");

        if (null != source) {
            target.setOrderNo(source.getCode());
            target.setDealerCode(source.getUser().getUid());
            target.setDealerName(source.getUser().getName());
            if (source instanceof CartModel)
                target.setStatus("Draft");
            else
                target.setStatus(String.valueOf(source.getStatus()));
            if (source.getDeliveryAddress() != null) {
                target.setDeliveryAddress(addressConverter.convert(source.getDeliveryAddress()));
            }
            if(source.getDestination()!=null) {
                target.setDestinationCode(source.getDestination().getCode());
                target.setDestinationName(source.getDestination().getName());
            }
            if(source.getSubAreaMaster()!=null) {
                target.setSubAreaMasterId(source.getSubAreaMaster().getPk().toString());
            }
            target.setProductCode(source.getProductCode());
            target.setProductName(source.getProductName());
            if(source.getTotalQuantity()!=null) {
                target.setQuantity(source.getTotalQuantity());
            }
            if (source.getStatus() != null && (source.getStatus().equals(OrderStatus.ORDER_SENT_TO_SO) || source.getStatus().equals(OrderStatus.ORDER_RECEIVED) || source.getStatus().equals(OrderStatus.ORDER_FAILED_VALIDATION) || source.getStatus().equals(OrderStatus.ORDER_MODIFIED))) {
                target.setRejectionReason(source.getRejectionReasons()!=null? source.getRejectionReasons().stream().collect(Collectors.joining(",")): StringUtils.EMPTY);
            }
            if (source.getTotalPrice() != null) {
                BigDecimal totalPrice = BigDecimal.valueOf(source.getTotalPrice().doubleValue());
                if (Boolean.TRUE.equals(source.getNet())) {
                    totalPrice = totalPrice.add(BigDecimal.valueOf(source.getTotalTax().doubleValue()));
                }
                target.setTotalPrice(priceDataFactory.create(PriceDataType.BUY, totalPrice, source.getCurrency()));
            }

        /*try {
            Date orderDate = new SimpleDateFormat("dd MMMM yyyy HH:mm").parse(order.getDate().toString());
            target.setOrderDate(orderDate);
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("Date is not in the correct format: " + e.getMessage());
        }*/

            target.setOrderDate(source.getDate());

            if (null != source.getUser().getProfilePicture()) {
                populateProfilePicture(source.getUser().getProfilePicture(), target);
            }
            target.setSpApprovalStatus(source.getSpApprovalStatus()!=null?source.getSpApprovalStatus().getCode():"");

            if(source.getCancelledDate()!=null) {
                target.setCancelledDate(source.getCancelledDate());
            }

        }
    }
    private void populateProfilePicture ( final MediaModel profilePicture,
                                          final NuvocoOrderHistoryData orderHistoryData){
        final ImageData profileImageData = imageConverter.convert(profilePicture);
        orderHistoryData.setDealerProfilePicture(profileImageData);
    }

}
