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
	public static final Integer QUANTITY_INMT_TO_BAGS = 20;

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

		public static final String SALES_OFFICER_GROUP_ID = "salesofficergroup";
		public static final String TSM_GROUP_ID = "tsmgroup";
		public static final String RH_GROUP_ID = "rhgroup";

	}


	public static class ORDER {
		public static final String PENDING_FOR_APPROVAL_STATUS = "PENDING_FOR_APPROVAL";
		public static final String WAITING_FOR_DISPATCH_STATUS = "WAITING_FOR_DISPATCH";
		public static final String TO_BE_DELIVERED_BY_TODAY_STATUS = "TO_BE_DELIVERED_BY_TODAY";
		public static final String VEHICLE_ARRIVAL_CONFIRMATION_STATUS = "VEHICLE_ARRIVAL_CONFIRMATION";

		public static final String PENDING_FOR_APPROVAL_STATUS_MAPPING = "mapping.statuses.pending.for.approval";
		public static final String WAITING_FOR_DISPATCH_STATUS_MAPPING = "mapping.statuses.waiting.for.dispatch";
		public static final String TO_BE_DELIVERED_BY_TODAY_STATUS_MAPPING = "mapping.statuses.to.be.delivered.today";
		public static final String VEHICLE_ARRIVAL_CONFIRMATION_STATUS_MAPPING = "mapping.statuses.vehicle.arrival.confirmation";

		public static final String NO_MATCHING_STATUS_ERROR = "Not a valid order status";
		public static final String ENUM_VALUES_SEPARATOR = ",";

		public static final String ORDER_CANCELLATION_STATUS = "CANCELLATION";
		public static final String ORDER_CANCELLATION_STATUS_MAPPING = "mapping.statuses.order.cancel";

		public static final String ORDER_LINE_CANCELLATION_STATUS = "LINE_CANCELLATION";
		public static final String ORDER_LINE_CANCELLATION_STATUS_MAPPING = "mapping.statuses.order.line.cancel";

	}

}
