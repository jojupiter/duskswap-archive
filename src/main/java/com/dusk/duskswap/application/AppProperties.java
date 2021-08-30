package com.dusk.duskswap.application;

public class AppProperties {

    // ====================== BTCPAY PROPERTIES =========================
    public static final String BTCPAY_RECEIVE_API = "71108d2c1722443951e445849678ed01591c64f1"; // Api to create invoice and receive cryptos from clients
    public static final String BTCPAY_SEND_API = "1c8d064550bdbabf88df49c87bfa521bc3df62ce"; // Api to send cryptos to clients (used in withdrawals)
    public static final String BTCPAY_RECEIVE_STORE_ID = "6pRqHdao7ne75ggFmthQ8eLXMZaChmH2xzttAzgHTHXu";
    public static final String BTCPAY_SEND_STORE_ID = "7bywVRvk6gFFbczcQq9vokad51z5qxgZ4RVrtzXcRe1t";

    // ====================== MONGO DB CONFIGS =========================
    public static final String MONGO_DATABASE_NAME = "duskbinance";
    public static final String MONGO_DATABASE_PORT = "27017";

}
