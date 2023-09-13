package com.nuvoco.core.services.impl;

import com.nuvoco.core.model.NuvocoCustomerModel;
import com.nuvoco.core.services.TerritoryManagementService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TerritoryManagementServiceImpl implements TerritoryManagementService {

    @Autowired
    UserService userService;

    @Autowired
    BaseSiteService baseSiteService;

    /**
     * @return
     */
    @Override
    public List<NuvocoCustomerModel> getRetailerListForDealer() {

        NuvocoCustomerModel customerModel=(NuvocoCustomerModel) userService.getCurrentUser();
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();

        return null;
    }
}
