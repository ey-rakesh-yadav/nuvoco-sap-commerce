/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.nuvoco.core.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import com.nuvoco.core.constants.NuvocoCoreConstants;
import com.nuvoco.core.setup.CoreSystemSetup;


/**
 * Do not use, please use {@link CoreSystemSetup} instead.
 * 
 */
public class NuvocoCoreManager extends GeneratedNuvocoCoreManager
{
	public static final NuvocoCoreManager getInstance()
	{
		final ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (NuvocoCoreManager) em.getExtension(NuvocoCoreConstants.EXTENSIONNAME);
	}
}
