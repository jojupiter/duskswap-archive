package com.dusk.externalAPIs.cinetpay.constants;

import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;

public class CinetpayParams {

    public static final String API_NAME = "CINETPAY";
    public static final String API_KEY = "";
    public static final String SITE_ID = "";
    public static final String RETURN_URL = "";

    public static final String STATUS_CREATED = "201";
    public static final String STATUS_SUCCESS = "00";

    public static final String CINETPAY_PAYMENT_FEES_CM = "0.025";
    public static final String CINETPAY_TRANSFER_FEES_CM = "0.02";
    public static String NOTIFICATION_URL = DefaultProperties.SERVER_BASE_URL + "/buy/check-status";
    public static String DEFAULT_PAYMENT_DESCRIPTION = "Paiement sur Duskswap";

}
