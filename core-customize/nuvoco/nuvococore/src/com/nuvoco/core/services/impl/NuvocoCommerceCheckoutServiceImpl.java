package com.nuvoco.core.services.impl;

import com.nuvoco.core.dao.NuvocoWarehouseDao;
import com.nuvoco.core.enums.DeliverySlots;
import com.nuvoco.core.enums.OrderType;
import com.nuvoco.core.model.*;
import com.nuvoco.core.services.NuvocoCommerceCheckoutService;
import com.nuvoco.core.services.TerritoryManagementService;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceCheckoutService;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

public class NuvocoCommerceCheckoutServiceImpl  extends DefaultCommerceCheckoutService implements NuvocoCommerceCheckoutService {


    @Autowired
    UserService userService;

    @Autowired
    ModelService modelService;

    @Autowired
    NuvocoWarehouseDao nuvocoWarehouseDao;

    @Autowired
    TerritoryManagementService territoryManagementService;
    /**
     * @param parameter
     * @return
     */
    @Override
    public boolean setCartDetails(CommerceCheckoutParameter parameter) {
        final CartModel cartModel = parameter.getCart();
        SubAreaMasterModel subAreaMaster = null;
        DistrictMasterModel districtMaster = null;
        RegionMasterModel regionMaster = null;
        StateMasterModel stateMaster = null;

        validateParameterNotNull(cartModel, "Cart model cannot be null");
        cartModel.setOrderType(OrderType.valueOf(parameter.getOrderType()));
        cartModel.setIsDealerProvideOwnTransport(parameter.getIsDealerProvideOwnTransport());
        cartModel.setDeliveryMode(parameter.getDeliveryMode());
        cartModel.setPaymentType(CheckoutPaymentType.ACCOUNT);
        cartModel.setTotalQuantity(parameter.getTotalQuantity());
        cartModel.setWarehouse(nuvocoWarehouseDao.findWarehouseByCode(parameter.getOrderSource()));
        if(null != parameter.getRetailerCode()) {
            cartModel.setRetailer((NuvocoCustomerModel) userService.getUserForUID(parameter.getRetailerCode()));
        }
        cartModel.setProductCode(parameter.getProductCode());
        cartModel.setProductName(parameter.getProductName());
        if(parameter.getOrderType().equals(OrderType.ISO.getCode()) && null != parameter.getDestination()) {
            cartModel.setDestination(nuvocoWarehouseDao.findWarehouseByCode(parameter.getDestination()));
        }
        cartModel.setRouteId(parameter.getRouteId());

        if(OrderType.ISO.getCode().equals(parameter.getOrderType())) {
            final AddressModel addressModel = parameter.getAddress();
            cartModel.setDeliveryAddress(addressModel);
            addressModel.setShippingAddress(Boolean.TRUE);
            modelService.save(addressModel);
        }
        if(cartModel.getUser() instanceof NuvocoCustomerModel) {
            List<SubAreaMasterModel> subareas = territoryManagementService.getTerritoriesForCustomer((NuvocoCustomerModel)cartModel.getUser());
            if(subareas!=null && !subareas.isEmpty()) {
                subAreaMaster = subareas.get(0);
                if(subAreaMaster!=null) {
                    cartModel.setSubAreaMaster(subAreaMaster);
                    districtMaster = subAreaMaster.getDistrictMaster();
                    if(districtMaster!=null) {
                        cartModel.setDistrictMaster(districtMaster);
                        regionMaster = districtMaster.getRegion();
                        if(regionMaster!=null) {
                            cartModel.setRegionMaster(regionMaster);
                            stateMaster = regionMaster.getState();
                            if(stateMaster!=null){
                                cartModel.setStateMaster(stateMaster);
                            }
                        }
                    }
                }
            }

        }
        //commented for poc
       /* List<FreightAndIncoTermsMasterModel> freightAndIncoTerms = findFreightAndIncoTerms(cartModel.getDeliveryAddress().getState(), cartModel.getDeliveryAddress().getDistrict(),
                cartModel.getSite(), cartModel.getWarehouse().getType().getCode());
        IncoTerms incoTerm = null;
        FreightTerms freightTerm = null;
        if(freightAndIncoTerms != null && !freightAndIncoTerms.isEmpty()) {
            for (FreightAndIncoTermsMasterModel f : freightAndIncoTerms) {
                incoTerm = IncoTerms.valueOf(f.getIncoTerms());
                freightTerm = FreightTerms.valueOf(f.getFrieghtTerms());
                break;
            }
        }*/

        if(cartModel.getEntries()!=null) {
            for(AbstractOrderEntryModel cartEntryModel : cartModel.getEntries()) {
                cartEntryModel.setSubAreaMaster(subAreaMaster);
                cartEntryModel.setDistrictMaster(districtMaster);
                cartEntryModel.setRegionMaster(regionMaster);
                cartEntryModel.setStateMaster(stateMaster);
                cartEntryModel.setSite(cartModel.getSite());
                cartEntryModel.setUser(cartModel.getUser());

               // cartEntryModel.setFob(incoTerm);
               // cartEntryModel.setFreightTerms(freightTerm);
               /* if(cartEntryModel.getFob()!=null && cartEntryModel.getFob().equals(IncoTerms.EX)) {
                    cartEntryModel.setEpodCompleted(true);
                }*/
             /*   if(cartEntryModel.getProduct()!=null && cartModel.getDeliveryMode()!=null && cartModel.getDeliveryAddress()!=null && cartModel.getSite()!=null) {
                    DestinationSourceMasterModel destinationSource =  destinationSourceMasterDao.getDestinationSourceBySource(OrderType.SO,  CustomerCategory.TR, cartEntryModel.getSource(), cartModel.getDeliveryMode(), cartModel.getDeliveryAddress().getErpCity(), cartModel.getDeliveryAddress().getDistrict(), cartModel.getDeliveryAddress().getState(), cartModel.getSite(),
                            cartEntryModel.getProduct().getGrade(), cartEntryModel.getProduct().getBagType(), cartModel.getDeliveryAddress().getTaluka());
                    if(destinationSource!=null && destinationSource.getDistance()!=null) {
                        cartEntryModel.setDistance(destinationSource.getDistance().doubleValue());
                    }
                }*/
                modelService.save(cartEntryModel);
            }
        }
        CustDepotMasterModel custDepotForCustomer =  territoryManagementService.getCustDepotForCustomer((NuvocoCustomerModel)cartModel.getUser());
        cartModel.setCustDepot(custDepotForCustomer);

        if(parameter.getRequestedDeliverySlot()!=null)
            cartModel.setRequestedDeliveryslot(DeliverySlots.valueOf(parameter.getRequestedDeliverySlot()));
        if(parameter.getRequestedDeliveryDate()!=null)
            cartModel.setRequestedDeliveryDate(parameter.getRequestedDeliveryDate());

        modelService.save(cartModel);

        final CommerceCartParameter commerceCartParameter = new CommerceCartParameter();
        commerceCartParameter.setEnableHooks(true);
        commerceCartParameter.setCart(cartModel);
        getCommerceCartCalculationStrategy().calculateCart(commerceCartParameter);
        return true;
    }
}
