/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.nuvoco.constants;

public class NuvocooccConstants extends GeneratedNuvocooccConstants
{
	public static final String EXTENSIONNAME = "nuvocoocc";

	public static final String OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH = "#{ ${occ.rewrite.overlapping.paths.enabled:false} ? '/{baseSiteId}/orgUsers/{userId}' : '/{baseSiteId}/users/{userId}'}";
	private NuvocooccConstants()
	{
		//empty
	}
}

