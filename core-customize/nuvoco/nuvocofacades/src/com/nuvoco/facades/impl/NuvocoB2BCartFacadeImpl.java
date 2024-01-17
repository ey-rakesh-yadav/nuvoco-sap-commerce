package com.nuvoco.facades.impl;

import com.nuvoco.core.services.NuvocoCartService;
import com.nuvoco.facades.NuvocoB2BCartFacade;
import com.nuvoco.facades.data.NuvocoOrderHistoryData;
import de.hybris.platform.b2bacceleratorfacades.exception.DomainException;
import de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException;
import de.hybris.platform.b2bacceleratorfacades.order.impl.DefaultB2BCartFacade;
import de.hybris.platform.commercefacades.order.data.AddToCartParams;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CommerceSaveCartParameterData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceCartService;
import de.hybris.platform.commerceservices.order.CommerceSaveCartException;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static de.hybris.platform.util.localization.Localization.getLocalizedString;

public class NuvocoB2BCartFacadeImpl extends DefaultB2BCartFacade implements NuvocoB2BCartFacade {


    private static final String BASKET_QUANTITY_ERROR_KEY = "basket.error.quantity.invalid";

    private static final String CART_MODIFICATION_ERROR = "basket.error.occurred";

    private static int DEFAULT_SAVE_CART_EXPIRY_DAYS = 30;


    @Autowired
    NuvocoCartService nuvocoCartService;

    @Autowired
    UserService userService;

    @Autowired
    ModelService modelService;


    @Autowired
    ConfigurationService configurationService;
    @Autowired
    private TimeService timeService;

    @Autowired
    private CommerceCartService commerceCartService;

    @Autowired
    Converter<AbstractOrderModel, NuvocoOrderHistoryData> nuvocoOrderHistoryCardConverter;

    @Override
    public CartModificationData addOrderEntry(final OrderEntryData cartEntry)
    {
        if (!isValidEntry(cartEntry))
        {
            throw new EntityValidationException(getLocalizedString(BASKET_QUANTITY_ERROR_KEY));
        }
        CartModificationData cartModification = null;

        final AddToCartParams addToCartParams = new AddToCartParams();
        addToCartParams.setProductCode(cartEntry.getProduct().getCode());
        addToCartParams.setQuantity(cartEntry.getQuantity());
        addToCartParams.setTruckNo(cartEntry.getTruckNo());
        addToCartParams.setDriverContactNo(cartEntry.getDriverContactNo());
        addToCartParams.setSelectedDeliveryDate(cartEntry.getSelectedDeliveryDate());
        addToCartParams.setSelectedDeliverySlot(cartEntry.getSelectedDeliverySlot());
        addToCartParams.setSequence(cartEntry.getSequence());
        addToCartParams.setCalculatedDeliveryDate(cartEntry.getCalculatedDeliveryDate());
        addToCartParams.setCalculatedDeliverySlot(cartEntry.getCalculatedDeliverySlot());
        addToCartParams.setQuantityMT(cartEntry.getQuantityMT());
        addToCartParams.setWarehouseCode(cartEntry.getWarehouseCode());
        addToCartParams.setRouteId(cartEntry.getRouteId());
        addToCartParams.setRemarks(cartEntry.getRemarks());
        try{
            cartModification = getCartFacade().addToCart(addToCartParams);
        }
        catch (final CommerceCartModificationException e)
        {
            throw new DomainException(getLocalizedString(CART_MODIFICATION_ERROR), e);
        }
        setAddStatusMessage(cartEntry, cartModification);
        return cartModification;
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

    /**
     * @param parameters
     * @param employeeCode
     * @return
     * @throws CommerceSaveCartException
     */
    @Override
    public boolean saveCart(CommerceSaveCartParameterData parameters, String employeeCode) throws CommerceSaveCartException {
        CartModel cartToBeSaved = null;

        if (StringUtils.isEmpty(parameters.getCartId()))
        {
            cartToBeSaved = getCartService().getSessionCart();
        }
        else
        {
            cartToBeSaved = commerceCartService.getCartForCodeAndUser(parameters.getCartId(),
                    userService.getCurrentUser());

            if (cartToBeSaved == null)
            {
                throw new CommerceSaveCartException("Cannot find a cart for code [" + parameters.getCartId() + "]");
            }
        }

        final Date currentDate = timeService.getCurrentTime();

        cartToBeSaved.setExpirationTime(calculateExpirationTime(currentDate));
        cartToBeSaved.setSaveTime(currentDate);
        cartToBeSaved.setSavedBy(userService.getUserForUID(employeeCode));
        cartToBeSaved.setName(parameters.getName());
        cartToBeSaved.setDescription(parameters.getDescription());

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
}
