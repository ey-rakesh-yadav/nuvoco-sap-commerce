package com.nuvoco.facades.populators;

import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.enums.TerritoryLevels;
import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.model.NuvocoUserModel;
import de.hybris.platform.commercefacades.order.converters.populator.AbstractOrderPopulator;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.PrincipalData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.warehousingfacades.storelocator.data.WarehouseData;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class NuvocoOrderDetailPopulator <T extends CartData> extends AbstractOrderPopulator<OrderModel, T> {


    @Autowired
    private EnumerationService enumerationService;

    @Autowired
    private Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter;

    @Autowired
    private Converter<AddressModel, AddressData> addressConverter;

    @Autowired
    private Converter<DeliveryModeModel, DeliveryModeData> deliveryModeConverter;

    @Autowired
    UserService userService;


    @Override
    public void populate(OrderModel source, T target) throws ConversionException {
        if (source.getOrderType() != null) {
            target.setOrderType(source.getOrderType().getCode());
        }
        target.setIsDealerProvideOwnTransport(source.getIsDealerProvideOwnTransport());
        if (source.getWarehouse() != null) {
            WarehouseData warehouse = new WarehouseData();
            warehouse.setCode(source.getWarehouse().getCode());
            target.setWarehouse(warehouse);
        }
        if (source.getDestination() != null) {
            target.setDestination(source.getDestination().getCode());
            target.setDestinationName(source.getDestination().getName());
        }
        if (source.getSubAreaMaster() != null) {
            target.setSubAreaCode(source.getSubAreaMaster().getPk().toString());
        }
        target.setProductName(source.getProductName());
        target.setProductCode(source.getProductCode());
        target.setTotalQuantity(source.getTotalQuantity());
        if (null != source.getRetailer()) {
            target.setRetailerName(source.getRetailer().getName());
            target.setRetailerCode(source.getRetailer().getUid());
        }
        if (source.getStatus() != null && source.getRejectionReasons() != null && (source.getStatus().equals(OrderStatus.ORDER_SENT_TO_SO) || source.getStatus().equals(OrderStatus.ORDER_RECEIVED) || source.getStatus().equals(OrderStatus.ORDER_FAILED_VALIDATION) || source.getStatus().equals(OrderStatus.ORDER_MODIFIED))) {
            target.setRejectionReasons(source.getRejectionReasons().stream().collect(Collectors.toList()));
            target.setSuggestionMap(source.getSuggestions());
        }
        if (source.getEntries() != null) {
            addEntries(source, target);
        }

        if (source.getUser() != null) {
            PrincipalData user = new PrincipalData();
            user.setUid(source.getUser().getUid());
            user.setName(source.getUser().getName());
            target.setUser(user);
        }

        if (source.getDeliveryMode() != null) {
            target.setDeliveryMode(deliveryModeConverter.convert(source.getDeliveryMode()));
        }
        if (source.getDeliveryAddress() != null) {
            target.setDeliveryAddress(addressConverter.convert(source.getDeliveryAddress()));
        }
        if (StringUtils.isNotBlank(source.getErpOrderNumber())) {
            target.setErpOrderNo(String.valueOf(source.getErpOrderNumber()));
        }

        if (source.getOrderAcceptedDate() == null)
            target.setUiStatus("PENDING_FOR_APPROVAL");
        else
            target.setUiStatus("ORDER_ACCEPTED");

        PriceData price = new PriceData();
        price.setValue(BigDecimal.valueOf(source.getTotalPrice()));
        target.setTotalPrice(price);

        target.setStatusDisplay(source.getStatusDisplay());
        target.setBankAccountNo(source.getUser() != null ? ((NuvocoCustomerModel) source.getUser()).getBankAccountNo() : null);

        if (null != source.getPlacedBy()) {
            target.setPlacedBy(source.getPlacedBy().getUid());
        }
        target.setCreditLimitBreached(source.getCreditLimitBreached());
        target.setSpApprovalStatus(source.getSpApprovalStatus() != null ? source.getSpApprovalStatus().getCode() : "");
        if (source.getRequestedDeliveryslot() != null)
        {
            target.setRequestedDeliverySlot(source.getRequestedDeliveryslot().getCode());
            target.setRequestDeliverySlotName(enumerationService.getEnumerationName(source.getRequestedDeliveryslot()));
        }

        if (source.getRequestedDeliveryDate() != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            target.setRequestedDeliveryDate(dateFormat.format(source.getRequestedDeliveryDate()));
        }

        if (userService.getCurrentUser() instanceof NuvocoUserModel) {
           /* NuvocoUserModel currentUser = (NuvocoUserModel) userService.getCurrentUser();
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID)) && source.getApprovalLevel() != null && source.getApprovalLevel().equals(TerritoryLevels.SUBAREA)) {
                target.setShowApprovalButton(true);
            } else if (currentUser.getGroups().contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.TSM_GROUP_ID)) && source.getApprovalLevel() != null && source.getApprovalLevel().equals(TerritoryLevels.DISTRICT)) {
                target.setShowApprovalButton(true);
            } else if (currentUser.getGroups().contains(userService.getUserGroupForUID(NuvocoCoreConstants.CUSTOMER.RH_GROUP_ID)) && source.getApprovalLevel() != null && source.getApprovalLevel().equals(TerritoryLevels.REGION)) {
                target.setShowApprovalButton(true);
            } else {
                target.setShowApprovalButton(false);
            }*/
        }
    }

    protected void addEntries(final AbstractOrderModel source, final AbstractOrderData prototype)
    {
        prototype.setEntries(getOrderEntryConverter().convertAll(source.getEntries()));
    }


}

