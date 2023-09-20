package com.nuvoco.core.services;

import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;

public interface NuvocoCommerceCheckoutService {

    boolean setCartDetails(CommerceCheckoutParameter parameter);
}
