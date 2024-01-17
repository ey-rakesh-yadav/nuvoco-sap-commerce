package com.nuvoco.facades;

import com.nuvoco.facades.data.DestinationSourceListData;
import com.nuvoco.facades.data.NuvocoOrderHistoryData;
import de.hybris.platform.commercefacades.order.data.CommerceSaveCartParameterData;
import de.hybris.platform.commerceservices.order.CommerceSaveCartException;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface NuvocoCartFacade {

    DestinationSourceListData fetchDestinationSource(String city, String orderType, String deliveryMode, String productCode, String district, String state, String taluka);

    SearchPageData<NuvocoOrderHistoryData> getSavedCartsBySavedBy(SearchPageData searchPageData, String filter, int month, int year, String productName, String orderType);

    boolean setOrderRequistionOnOrder(CartWsDTO cartDetails);

    boolean setCartDetails(CartWsDTO cartDetails);

    boolean saveCart(CommerceSaveCartParameterData parameters, String employeeCode) throws CommerceSaveCartException;
}
