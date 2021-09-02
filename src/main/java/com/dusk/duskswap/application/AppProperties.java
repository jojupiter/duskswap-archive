package com.dusk.duskswap.application;

public class AppProperties {

    // ====================== BTCPAY PROPERTIES =========================
    public static final String BTCPAY_RECEIVE_API = "1576f859fd3e9232db20aad69964ccc0dbc8959d "; // Api to create invoice and receive cryptos from clients
    public static final String BTCPAY_SEND_API = "1576f859fd3e9232db20aad69964ccc0dbc8959d"; // Api to send cryptos to clients (used in withdrawals)
    public static final String BTCPAY_RECEIVE_STORE_ID = "6pRqHdao7ne75ggFmthQ8eLXMZaChmH2xzttAzgHTHXu";
    public static final String BTCPAY_SEND_STORE_ID = "7bywVRvk6gFFbczcQq9vokad51z5qxgZ4RVrtzXcRe1t";
    public static final String BTCPAY_SERVER_DOMAIN_URL = "https://ax1.duskpay.com/api/v1/stores/";

    // ====================== MONGO DB CONFIGS =========================
    public static final String MONGO_DATABASE_NAME = "duskbinance";
    public static final String MONGO_DATABASE_PORT = "27017";

}
