package com.nuvoco.facades.populators;

import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.enums.TerritoryLevels;
import com.nuvoco.core.model.NuvocoUserModel;
import com.nuvoco.facades.data.NuvocoOrderData;
import com.nuvoco.facades.data.TrackingData;
import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.PrincipalData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NuvocoOrderCardPopulator implements Populator<OrderModel, NuvocoOrderData> {



    @Autowired
    private Converter<ProductModel, ProductData> productConverter;
    @Autowired
    private Converter<AddressModel, AddressData> addressConverter;
    @Autowired
    private Populator<OrderEntryModel, List<TrackingData>> trackingDetailsOnEntryPopulator;
    @Autowired
    private Populator<OrderModel, List<TrackingData>> trackingDetailsOnOrderPopulator;
    @Autowired
    Converter<MediaModel, ImageData> imageConverter;

    @Autowired
    EnumerationService enumerationService;

    @Autowired
    UserService userService;

    @Override
    public void populate(OrderModel source, NuvocoOrderData target) throws ConversionException {

        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");


        target.setCode(source.getCode());
        target.setStatus(source.getStatusDisplay());
        poplateProductAndQuantityFromOrderEntry(source,target);
        if(null != source.getDeliveryAddress()){
            populateDeliveryAddress(source.getDeliveryAddress(),target);
        }
        if(source.getSubAreaMaster()!=null) {
            target.setSubAreaMasterId(source.getSubAreaMaster().getPk().toString());
        }
        target.setEstimatedDeliveryDate(source.getEstimatedDeliveryDate());
        List<TrackingData> trackingDataList = new ArrayList<>();
        trackingDetailsOnOrderPopulator.populate(source,trackingDataList);

        if(source.getCancelledDate()!=null) {
            TrackingData cancelledTrackingData = new TrackingData();
            cancelledTrackingData.setCode(OrderStatus.CANCELLED.getCode());
            cancelledTrackingData.setName(enumerationService.getEnumerationName(OrderStatus.CANCELLED));
            cancelledTrackingData.setActualTime(source.getCancelledDate());
            cancelledTrackingData.setIndex(5);
            trackingDataList.add(cancelledTrackingData);
        }

        target.setTrackingDetails(trackingDataList);

        if (null != source.getUser().getProfilePicture()) {
            populateProfilePicture(source.getUser().getProfilePicture(), target);
        }

        PrincipalData user = new PrincipalData();
        user.setUid(source.getUser().getUid());
        user.setName(source.getUser().getName());
        target.setUser(user);

        if(source.getDestination()!=null) {
            target.setDestinationCode(source.getDestination().getCode());
            target.setDestinationName(source.getDestination().getName());
        }

        if(userService.getCurrentUser() instanceof NuvocoUserModel) {
            NuvocoUserModel currentUser = (NuvocoUserModel) userService.getCurrentUser();
            if(currentUser.getGroups().contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID)) && source.getApprovalLevel()!=null && source.getApprovalLevel().equals(TerritoryLevels.SUBAREA)) {
                target.setShowApprovalButton(true);
            } else if (currentUser.getGroups().contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.TSM_GROUP_ID)) && source.getApprovalLevel()!=null && source.getApprovalLevel().equals(TerritoryLevels.DISTRICT)) {
                target.setShowApprovalButton(true);
            } else if (currentUser.getGroups().contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.RH_GROUP_ID)) && source.getApprovalLevel()!=null && source.getApprovalLevel().equals(TerritoryLevels.REGION)) {
                target.setShowApprovalButton(true);
            }
            else {
                target.setShowApprovalButton(false);
            }

        }
    }

    void poplateProductAndQuantityFromOrderEntry(final OrderModel source ,final NuvocoOrderData target ){
        //  Double totalQuantity = (double) 0;
        List<ProductData> productDataList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(source.getEntries())){
//            Map<String, Double> productCodeQtyMap = source.getEntries().stream().collect(Collectors.groupingBy(e->e.getProduct().getCode(), Collectors.summingDouble(AbstractOrderEntryModel::getQuantityMT)));
//            for(var entry :productCodeQtyMap.entrySet()){
//                totalQuantity += entry.getValue();
//            }
            Set<ProductModel> productsFromOrder = source.getEntries().stream().filter(each->each.getProduct()!=null).map(AbstractOrderEntryModel::getProduct).collect(Collectors.toSet());

            if(CollectionUtils.isNotEmpty(productsFromOrder)){
                for(ProductModel productModel : productsFromOrder){
                    ProductData productData = getProductInfo(productModel);
                    productDataList.add(productData);
                }
            }
        }
        target.setProducts(productDataList);
        target.setQuantity(source.getTotalQuantity());
    }



    private void populateProfilePicture ( final MediaModel profilePicture,
                                          final NuvocoOrderData sclOrderData){
        final ImageData profileImageData = imageConverter.convert(profilePicture);
        sclOrderData.setDealerProfilePicture(profileImageData);
    }
    private ProductData getProductInfo(final ProductModel productModel){

        return productConverter.convert(productModel);
    }

    private void populateDeliveryAddress(final AddressModel deliveryAddress , final NuvocoOrderData target){
        target.setDeliveryAddress(addressConverter.convert(deliveryAddress));
    }
}
