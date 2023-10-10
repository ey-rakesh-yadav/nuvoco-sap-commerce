package com.nuvoco.core.dao;

import de.hybris.platform.notificationservices.model.SiteMessageModel;

public interface NuvocoSiteMessageDao {

    SiteMessageModel findSiteMessageById(String siteMessageId);
}
