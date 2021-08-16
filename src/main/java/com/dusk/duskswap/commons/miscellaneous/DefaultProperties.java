package com.dusk.duskswap.commons.miscellaneous;

public class DefaultProperties {
    // ========== roles ==================
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_SECRETARY = "ROLE_SECRETARY";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

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
    public static final String STATUS_TRANSACTION_CRYPTO_PROCESSING = "TRANSACTION_CRYPTO_Processing";
    public static final String STATUS_TRANSACTION_CRYPTO_SETTLED = "TRANSACTION_CRYPTO_Settled";
    public static final String STATUS_TRANSACTION_CRYPTO_INVALID = "TRANSACTION_CRYPTO_Invalid";
    public static final String STATUS_TRANSACTION_CRYPTO_COMPLETE = "TRANSACTION_CRYPTO_Complete";
    public static final String STATUS_TRANSACTION_CRYPTO_RADICAL = "TRANSACTION_CRYPTO_";

    // ============ levels ===============
    public static final String LEVEL_ISO_0 = "LVL_0";
    public static final String LEVEL_ISO_1 = "LVL_1";

    // ============ email addresses ===========
    public static final String EMAIL_NO_REPLY_ADDRESS = "Duskswap<no-reply@duskpay.com>";
    public static final String EMAIL_SUPPORT_ADDRESS = "Duskswap<support@duskpay.com>";
    public static final String EMAIL_NEWSLETTERS_ADDRESS = "Duskswap<newsletters@duskpay.com>";

    // ============ verification codes ==========
    public static final String VERIFICATION_WITHDRAWAL_SELL_PURPOSE = "WITHDRAWAL_SELL";
    public static final String VERIFICATION_SIGN_IN_UP_PURPOSE = "SIGN_IN_UP";
    public static final String VERIFICATION_FORGOT_PASSWORD = "FORGOT_PASSWORD";

    // =========== pages ========================
    public static final int DEFAULT_PAGE_SIZE = 10;

    // ========== notifications =================
    private static final String BUY_NOTIFICATION_URL_OM = "";

    // ============ pricing ==================
    public static final String PRICING_TYPE_FIX = "FIX";
    public static final String PRICING_TYPE_PERCENTAGE = "PERCENTAGE";
    public static final String PRICING_EURO_DEFAULT_VALUE_IN_XAF = "656";

    // =========== currency =====================
    public static final String CURRENCY_EUR_ISO = "EUR";
    public static final String CURRENCY_TYPE_CRYPTO = "CRYPTO";
    public static final String CURRENCY_TYPE_FIAT = "FIAT";

    // =========== btcpay params ================
    public static final String BTCPAY_INVOICE_LOW_SPEED = "LowSpeed";
    public static final String BTCPAY_INVOICE_MEDIUM_SPEED = "MediumSpeed";
    public static final String BTCPAY_INVOICE_LOW_MEDIUM_SPEED = "LowMediumSpeed";
    public static final String BTCPAY_INVOICE_HIGH_SPEED = "HighSpeed";

    public static final String BTCPAY_INVOICE_STATUS_PAID_OVER = "PaidOver";
    public static final String BTCPAY_INVOICE_STATUS_PAID_PARTIAL = "PaidPartial";
    public static final String BTCPAY_INVOICE_STATUS_PAID_LATE = "PaidLate";
    public static final String BTCPAY_INVOICE_STATUS_NONE = "None";
    public static final String BTCPAY_INVOICE_STATUS_MARKED = "Marked";

    // ========== deposits =====================
    public static final String DEPOSIT_DEFAULT_VALUE = "0.00001";

}
