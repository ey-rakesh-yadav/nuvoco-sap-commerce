package com.nuvoco.facades;

import com.nuvoco.facades.data.NuvocoOrderHistoryData;
import de.hybris.platform.commercefacades.order.data.CommerceSaveCartParameterData;
import de.hybris.platform.commerceservices.order.CommerceSaveCartException;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface NuvocoB2BCartFacade {


    public SearchPageData<NuvocoOrderHistoryData> getSavedCartsBySavedBy(SearchPageData searchPageData, String filter, int month, int year, String productName, String orderType);

    boolean saveCart(CommerceSaveCartParameterData parameters, String employeeCode) throws CommerceSaveCartException;

}
