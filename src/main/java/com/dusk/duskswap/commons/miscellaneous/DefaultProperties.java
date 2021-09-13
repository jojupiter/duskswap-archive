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
    public static final String BTCPAY_INVOICE_STATUS_COMPLETE = "Complete";
    public static final String BTCPAY_INVOICE_STATUS_SETTLED = "Settled";
    public static final String BTCPAY_INVOICE_STATUS_PROCESSING = "Processing";
    public static final String BTCPAY_INVOICE_STATUS_INVALID = "Invalid";

    // ========== deposits =====================
    public static final String DEPOSIT_DEFAULT_VALUE = "800000000";
    public static final int MAX_NUMBER_OF_TRANSACTION_FOR_INVOICE = 150;

    // ========== block explorer ================
    public static final int DEFAULT_BLOCK_TARGET = 3;
    public static final double MAX_BTC_SAT_PER_BYTES = 8;

    // ========= transaction size estimation ==========
    public static final int BTC_TRANSACTION_SIZE_MAX = 250; // for a normal transaction it's about 226vb(1 input: 148, 1 output: 68, overhead: 10 >> formula: size = nb_in * input + nb_out * output + overhead) but we take a maximum in order to not having problem
    public static final int DASH_TRANSACTION_SIZE_MAX = 0;
    public static final int ETH_TRANSACTION_SIZE_MAX = 0;
    public static final int LTC_TRANSACTION_SIZE_MAX = 0;
    public static final int DOGE_TRANSACTION_SIZE_MAX = 0;

}
