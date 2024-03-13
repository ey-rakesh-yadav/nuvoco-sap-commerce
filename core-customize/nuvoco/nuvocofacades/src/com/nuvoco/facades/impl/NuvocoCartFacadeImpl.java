package com.nuvoco.facades.impl;

import com.nuvoco.core.dao.*;
import com.nuvoco.core.enums.OrderType;
import com.nuvoco.core.model.DestinationSourceMasterModel;
import com.nuvoco.core.services.NuvocoCartService;
import com.nuvoco.core.services.NuvocoCommerceCheckoutService;
import com.nuvoco.facades.NuvocoCartFacade;
import com.nuvoco.facades.data.DestinationSourceListData;
import com.nuvoco.facades.data.DestinationSourceMasterData;
import com.nuvoco.facades.data.NuvocoOrderHistoryData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CommerceSaveCartParameterData;
import de.hybris.platform.commercefacades.order.impl.DefaultCartFacade;
import de.hybris.platform.commerceservices.order.CommerceCartService;
import de.hybris.platform.commerceservices.order.CommerceSaveCartException;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.log4j.Logger;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class NuvocoCartFacadeImpl  extends DefaultCartFacade implements NuvocoCartFacade {

    private static int DEFAULT_SAVE_CART_EXPIRY_DAYS = 30;
private static final Logger LOGGER = Logger.getLogger(NuvocoCartFacadeImpl.class);

	
    @Autowired
    NuvocoCartService nuvocoCartService;

    @Autowired
    UserService userService;

    @Autowired
    ModelService modelService;

    @Autowired
    TerritoryManagementDao territoryManagementDao;

    @Autowired
    DistrictMasterDao districtMasterDao;

    @Autowired
    StateMasterDao stateMasterDao;

    @Autowired
    RegionMasterDao regionMasterDao;

    @Autowired
    ProductService productService;

    @Autowired
    DepotOperationDao depotOperationDao;

    @Autowired
    private CommerceCartService commerceCartService;

    @Autowired
    private TimeService timeService;

    @Autowired
    NuvocoCommerceCheckoutService nuvocoCommerceCheckoutService;

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    Converter<AbstractOrderModel, NuvocoOrderHistoryData> nuvocoOrderHistoryCardConverter;

    @Autowired
    Converter<DestinationSourceMasterModel, DestinationSourceMasterData> destinationSourceConverter;
    /**
     * @param city
     * @param orderType
     * @param deliveryMode
     * @param productCode
     * @param district
     * @param state
     * @param taluka
     * @return
     */
    @Override
    public DestinationSourceListData fetchDestinationSource(String city, String orderType, String deliveryMode, String productCode, String district, String state, String taluka) {
        DestinationSourceListData sourceListData = new DestinationSourceListData();
        List<DestinationSourceMasterModel> destinationSourceMasterList = nuvocoCartService.fetchDestinationSourceByCity(city, orderType, deliveryMode,productCode, district, state, taluka);
        if( destinationSourceMasterList !=null && !destinationSourceMasterList.isEmpty()) {
            List<DestinationSourceMasterData> destinationSourceMasterData = destinationSourceConverter.convertAll(destinationSourceMasterList);
            LOG.info(String.format("Destination Source Master Data List Afger Converter Call ::%s",destinationSourceMasterData.size()));
            destinationSourceMasterData.sort(Comparator.comparing(DestinationSourceMasterData::getPriority));
            sourceListData.setDestinationSourceDataList(destinationSourceMasterData);

            Optional<DestinationSourceMasterData> sourceMaster = destinationSourceMasterData.stream().filter(d -> d.getSourcePriority().equals("L1")).findAny();
              LOG.info(String.format("Destination Source Master Data List Afger Filter  ::%s",sourceMaster.get().getCity()));
            if (sourceMaster.isPresent()) {
                sourceListData.setDefaultSource(sourceMaster.get());
            }
        }
        return sourceListData;
    }

    /**
     * @param searchPageData
     * @param filter
     * @param month
     * @param year
     * @param productName
     * @param orderType
     * @return
     */
    @Override
    public SearchPageData<NuvocoOrderHistoryData> getSavedCartsBySavedBy(SearchPageData searchPageData, String filter, int month, int year, String productName, String orderType) {
        final SearchPageData<NuvocoOrderHistoryData> result = new SearchPageData<>();
       final SearchPageData<CartModel> savedCartModels = nuvocoCartService.getSavedCartsBySavedBy(userService.getCurrentUser(), searchPageData, filter, month, year,productName,orderType);

       result.setPagination(savedCartModels.getPagination());
        result.setSorts(savedCartModels.getSorts());

       List<NuvocoOrderHistoryData> orderHistoryData = nuvocoOrderHistoryCardConverter.convertAll(savedCartModels.getResults());

       // final List<CartData> savedCartDatas = Converters.convertAll(savedCartModels.getResults(), getCartConverter());

        result.setResults(orderHistoryData);
        return result;
    }

    @Override
    public boolean setCartDetails(CartWsDTO cartDetails) {
        validateParameterNotNullStandardMessage("cartDetails", cartDetails);

        final CartModel cartModel = getCart();
        if(null != cartModel)
        {
            final DeliveryModeModel deliveryModeModel = getDeliveryService().getDeliveryModeForCode(cartDetails.getDeliveryMode().getCode());

            final CommerceCheckoutParameter parameter = createCommerceCheckoutParameter(cartModel, true);
            parameter.setOrderSource(cartDetails.getOrderSource());
            parameter.setOrderType(cartDetails.getOrderType());
            parameter.setIsDealerProvideOwnTransport(cartDetails.getIsDealerProvideOwnTransport());
            parameter.setDeliveryMode(deliveryModeModel);
            parameter.setTotalQuantity(cartDetails.getTotalQuantity());
            parameter.setRetailerCode(cartDetails.getRetailerCode());
            parameter.setProductCode(cartDetails.getProductCode());
            if(cartDetails.getProductCode() != null)
            {
                ProductModel product= productService.getProductForCode(cartDetails.getProductCode());
                parameter.setProductName(product.getName());
            }
            parameter.setDestination(cartDetails.getDestination());
            parameter.setRouteId(cartDetails.getRouteId());
            if(OrderType.ISO.getCode().equals(cartDetails.getOrderType())) {
                AddressModel addressModel = depotOperationDao.findDepotAddressByPk(cartDetails.getDeliveryAddress().getId());
                parameter.setAddress(addressModel);
                parameter.setIsDeliveryAddress(true);
            }
            if(cartDetails.getRequestedDeliveryDate()!=null)
                parameter.setRequestedDeliveryDate(setRequestedDeliveryDate(cartDetails.getRequestedDeliveryDate()));
            if(cartDetails.getRequestedDeliverySlot()!=null)
                parameter.setRequestedDeliverySlot(cartDetails.getRequestedDeliverySlot());
            return nuvocoCommerceCheckoutService.setCartDetails(parameter);
        }
        return false;
    }

    /**
     * @param inputParameters
     * @param employeeCode
     * @return
     * @throws CommerceSaveCartException
     */
    @Override
    public boolean saveCart(CommerceSaveCartParameterData inputParameters, String employeeCode) throws CommerceSaveCartException {
        CartModel cartToBeSaved = null;

        if (StringUtils.isEmpty(inputParameters.getCartId()))
        {
            cartToBeSaved = getCartService().getSessionCart();
        }
        else
        {
            cartToBeSaved = commerceCartService.getCartForCodeAndUser(inputParameters.getCartId(),
                    getUserService().getCurrentUser());

            if (cartToBeSaved == null)
            {
                throw new CommerceSaveCartException("Cannot find a cart for code [" + inputParameters.getCartId() + "]");
            }
        }

        final Date currentDate = timeService.getCurrentTime();

        cartToBeSaved.setExpirationTime(calculateExpirationTime(currentDate));
        cartToBeSaved.setSaveTime(currentDate);
        cartToBeSaved.setSavedBy(userService.getUserForUID(employeeCode));
        cartToBeSaved.setName(inputParameters.getName());
        cartToBeSaved.setDescription(inputParameters.getDescription());

        //saveCartResult.setSavedCart(cartToBeSaved);
        modelService.save(cartToBeSaved);
        modelService.refresh(cartToBeSaved);

        return true;
    }


    protected Date calculateExpirationTime(final Date currentDate)
    {
        final Integer expirationDays = configurationService.getConfiguration().getInteger(
                "commerceservices.saveCart.expiryTime.days", Integer.valueOf(DEFAULT_SAVE_CART_EXPIRY_DAYS));
        return new DateTime(currentDate).plusDays(expirationDays.intValue()).toDate();
    }
    private Date setRequestedDeliveryDate(String requestedDeliveryDate) {
        Date date=null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(requestedDeliveryDate);
            return date;
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Please provide valid date %s", requestedDeliveryDate));
        }
    }


    protected CommerceCheckoutParameter createCommerceCheckoutParameter(final CartModel cart, final boolean enableHooks)
    {
        final CommerceCheckoutParameter parameter = new CommerceCheckoutParameter();
        parameter.setEnableHooks(enableHooks);
        parameter.setCart(cart);
        return parameter;
    }
    /**
     * @param cartDetails
     * @return
     */
    @Override
    public boolean setOrderRequistionOnOrder(CartWsDTO cartDetails) {
        validateParameterNotNullStandardMessage("cartDetails", cartDetails);

        final CartModel cartModel = getCart();
        if(null != cartModel)
        {
            List<String> requistionList = cartDetails.getOrderRequistions();
            if(requistionList!=null) {
                cartModel.setRequisitionNumberList(requistionList);
                modelService.save(cartModel);
            }
            if(cartDetails.getDistrictCode()!=null){
                cartModel.setDistrictMaster(districtMasterDao.findByCode(cartDetails.getDistrictCode()));
            }
            else{

            }
            if(cartDetails.getSubAreaCode()!=null){
                cartModel.setSubAreaMaster(territoryManagementDao.getTerritoryById(cartDetails.getSubAreaCode()));
            }
            else{

            }
            if(cartDetails.getStateCode()!=null){
                cartModel.setStateMaster(stateMasterDao.findByCode(cartDetails.getStateCode()));
            }
            else{

            }
            if(cartDetails.getRegionCode()!=null){
                cartModel.setRegionMaster(regionMasterDao.findByCode(cartDetails.getRegionCode()));
            }
            else{

            }
            modelService.save(cartModel);
        }

        else {
            throw new IllegalArgumentException("Cart not found");
        }
        return false;
    }

    protected CartModel getCart()
    {
        return hasSessionCart() ? getCartService().getSessionCart() : null;
    }
}
