package com.nuvoco.controllers;

import com.nuvoco.facades.NuvocoNotificationFacade;
import com.nuvoco.security.NuvocoSecuredAccessConstants;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

public class NuvocoNotificationController extends NuvocooccController{


    @Autowired
    NuvocoNotificationFacade nuvocoNotificationFacade;

    @Secured({ NuvocoSecuredAccessConstants.ROLE_B2BADMINGROUP, NuvocoSecuredAccessConstants.ROLE_TRUSTED_CLIENT,NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP,NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/updateNotificationStatus/{siteMessageId}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public boolean updateNotificationStatus(@PathVariable String siteMessageId)
    {
        return nuvocoNotificationFacade.updateNotificationStatus(siteMessageId);
    }
}
