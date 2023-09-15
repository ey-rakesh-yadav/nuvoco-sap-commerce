/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.nuvoco.core.constants;

/**
 * Global class for all NuvocoCore constants. You can add global constants for your extension into this class.
 */
public final class NuvocoCoreConstants extends GeneratedNuvocoCoreConstants
{
	public static final String EXTENSIONNAME = "nuvococore";

	public static final String UNDERSCORE_CHARACTER = "_";

	public static final String IMAGE_MEDIA_FOLDER_NAME = "images";
	private NuvocoCoreConstants()
	{
		//empty
	}

	// implement here constants used by this extension
	public static final String QUOTE_BUYER_PROCESS = "quote-buyer-process";
	public static final String QUOTE_SALES_REP_PROCESS = "quote-salesrep-process";
	public static final String QUOTE_USER_TYPE = "QUOTE_USER_TYPE";
	public static final String QUOTE_SELLER_APPROVER_PROCESS = "quote-seller-approval-process";
	public static final String QUOTE_TO_EXPIRE_SOON_EMAIL_PROCESS = "quote-to-expire-soon-email-process";
	public static final String QUOTE_EXPIRED_EMAIL_PROCESS = "quote-expired-email-process";
	public static final String QUOTE_POST_CANCELLATION_PROCESS = "quote-post-cancellation-process";

	public static class CUSTOMER {
		public static final String DEALER_USER_GROUP_UID = "NuvocoDealerGroup";
		public static final String SITE_USER_GROUP_UID = "NuvocoSiteGroup";
		public static final String RETAILER_USER_GROUP_UID = "NuvocoRetailerGroup";
		public static final String SALES_PROMOTER_USER_GROUP_UID = "salespromotergroup";

		public static final String DEALER_USER_GROUP_TYPE = "Dealer";
		public static final String SITE_USER_GROUP_TYPE = "Site";
		public static final String RETAILER_USER_GROUP_TYPE = "Retailer";
		public static final String DEFAULT_Nuvoco_CUSTOMER_UNIT = "NuvocoCustomerUnit";

		public static final String DEALER_ONBOARDING_USER_GROUP_UID = "NuvocoDealerOnboardingGroup";
		public static final String RETAILER_ONBOARDING_USER_GROUP_UID = "NuvocoRetailerOnboardingGroup";

	}

}
