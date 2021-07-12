package com.dusk.duskswap.commons.miscellaneous;

public class DefaultProperties {
    // =========== statuses ==============
    public static final String STATUS_USER_SUSPENDED_BY_SUPERADMIN = "USER_SUSPENDED_BY_SUPERADMIN";
    public static final String STATUS_USER_SELF_SUSPENDED = "USER_SELF_SUSPENDED";
    public static final String STATUS_USER_ACTIVATED = "USER_ACTIVATED";
    public static final String STATUS_USER_DISABLED = "USER_DISABLED";
    public static final String STATUS_ENTERPRISE_NULL = "ENTERPRISE_NULL";
    public static final String STATUS_ENTERPRISE_NOT_VERIFIED = "ENTERPRISE_NOT_VERIFIED";
    public static final String STATUS_ENTERPRISE_BEING_VERIFIED = "ENTERPRISE_BEING_VERIFIED";
    public static final String STATUS_ENTERPRISE_VERIFIED = "ENTERPRISE_VERIFIED";
    public static final String STATUS_TRANSACTION_CONFIRMED = "TRANSACTION_CONFIRMED";
    public static final String STATUS_TRANSACTION_IN_CONFIRMATION = "TRANSACTION_IN_CONFIRMATION";
    public static final String STATUS_TRANSACTION_INITIATED = "TRANSACTION_INITIATED";
    public static final String STATUS_TRANSACTION_CRYPTO_NEW = "TRANSACTION_CRYPTO_New";
    public static final String STATUS_TRANSACTION_CRYPTO_EXPIRED = "TRANSACTION_CRYPTO_Expired";
    public static final String STATUS_TRANSACTION_CRYPTO_PAID = "TRANSACTION_CRYPTO_Paid";
    public static final String STATUS_TRANSACTION_CRYPTO_CONFIRMED = "TRANSACTION_CRYPTO_Confirmed";
    public static final String STATUS_TRANSACTION_CRYPTO_COMPLETE = "TRANSACTION_CRYPTO_Complete";
    public static final String STATUS_TRANSACTION_CRYPTO_INVALID = "TRANSACTION_CRYPTO_Invalid";
    public static final String STATUS_TRANSACTION_CRYPTO_RADICAL = "TRANSACTION_CRYPTO_";

    // ============ email addresses ===========
    public static final String EMAIL_NO_REPLY_ADDRESS = "Duskswap<no-reply@duskpay.com>";
    public static final String EMAIL_SUPPORT_ADDRESS = "Duskswap<support@duskpay.com>";
    public static final String EMAIL_NEWSLETTERS_ADDRESS = "Duskswap<newsletters@duskpay.com>";

    // ============ verification codes ==========
    public static final String VERIFICATION_WITHDRAWAL_SELL_PURPOSE = "WITHDRAWAL_SELL";
    public static final String VERIFICATION_SIGN_IN_UP_PURPOSE = "SIGN_IN_UP";

    // =========== pages ========================
    public static final int DEFAULT_PAGE_SIZE = 10;

    // ========== notifications =================
    private static final String BUY_NOTIFICATION_URL_OM = "";

    // ============ pricing ==================
    public static final String PRICING_TYPE_FIX = "fix";
    public static final String PRICING_TYPE_PERCENTAGE = "percentage";
}
