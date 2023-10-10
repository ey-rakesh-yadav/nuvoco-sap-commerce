package com.nuvoco.facades.impl;

import com.nuvoco.core.services.NuvocoNotificationService;
import com.nuvoco.facades.NuvocoNotificationFacade;
import org.springframework.beans.factory.annotation.Autowired;

public class NuvocoNotificationFacadeImpl implements NuvocoNotificationFacade {


    @Autowired
    NuvocoNotificationService notificationService;

    /**
     * @param siteMessageId
     * @return
     */
    @Override
    public boolean updateNotificationStatus(String siteMessageId) {
        return notificationService.updateNotificationStatus(siteMessageId);
    }
}
