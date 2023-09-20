package com.nuvoco.core.services;

import com.nuvoco.core.model.DestinationSourceMasterModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface NuvocoCartService {

    List<DestinationSourceMasterModel> fetchDestinationSourceByCity(String city, String orderType, String deliveryMode, String productCode, String district, String state, String taluka);

    SearchPageData<CartModel> getSavedCartsBySavedBy(UserModel user, SearchPageData searchPageData, String filter, int month, int year, String productName , String orderType);
}
