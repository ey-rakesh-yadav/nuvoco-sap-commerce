/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.nuvoco.fulfilmentprocess.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import com.nuvoco.fulfilmentprocess.constants.NuvocoFulfilmentProcessConstants;

public class NuvocoFulfilmentProcessManager extends GeneratedNuvocoFulfilmentProcessManager
{
	public static final NuvocoFulfilmentProcessManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (NuvocoFulfilmentProcessManager) em.getExtension(NuvocoFulfilmentProcessConstants.EXTENSIONNAME);
	}
	
}
