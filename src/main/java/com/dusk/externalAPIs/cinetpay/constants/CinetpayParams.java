package com.dusk.externalAPIs.cinetpay.constants;

import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;

public class CinetpayParams {

    public static final String API_NAME = "CINETPAY";
    public static final String API_KEY = "123104891961025e41440769.12175230";
    public static final String SITE_ID = "951926";
    public static final String RETURN_URL = "";

    public static final String STATUS_CREATED = "201";
    public static final String STATUS_PAYMENT_SUCCESS = "00";
    public static final String STATUS_PAYMENT_FAILED = "600";
    public static final String STATUS_PAYMENT_CANCELED = "627";
    public static final String STATUS_UNKNOWN_ERROR = "624";
    public static final String STATUS_AMOUNT_TOO_LOW = "641";
    public static final String STATUS_AMOUNT_TOO_HIGH = "642";
    public static final int STATUS_TRANSFER_SUCCESS = 0;
    public static final String STATUS_TRANSFER_TREATMENT_VAL = "VAL";
    public static final String STATUS_TRANSFER_TREATMENT_REJ = "REJ";

    public static final String CINETPAY_PAYMENT_FEES_CM = "0.025";
    public static final String CINETPAY_TRANSFER_FEES_CM = "0.02";
    public static String PAYMENT_NOTIFICATION_URL = DefaultProperties.SERVER_BASE_URL + "/buy/check-status";
    public static String TRANSFER_NOTIFICATION_URL = DefaultProperties.SERVER_BASE_URL + "/sell/check-status";
    public static String DEFAULT_PAYMENT_DESCRIPTION = "Paiement sur Duskswap";
    public static String TRANSFER_PASSWORD = "fGooGA@se7h901aRI";

}
