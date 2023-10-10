package com.nuvoco.core.services.impl;

import com.nuvoco.core.dao.NuvocoSiteMessageDao;
import com.nuvoco.core.enums.NotificationStatus;
import com.nuvoco.core.services.NuvocoNotificationService;
import de.hybris.platform.notificationservices.model.SiteMessageModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.springframework.beans.factory.annotation.Autowired;

public class NuvocoNotificationServiceImpl implements NuvocoNotificationService {


    @Autowired
    NuvocoSiteMessageDao nuvocoSiteMessageDao;

    @Autowired
    ModelService modelService;

    /**
     * @param siteMessageId
     * @return
     */
    @Override
    public boolean updateNotificationStatus(String siteMessageId) {
        SiteMessageModel siteMessage = nuvocoSiteMessageDao.findSiteMessageById(siteMessageId);
        siteMessage.setStatus(NotificationStatus.READ);
        modelService.save(siteMessage);
        return true;
    }
}
