package com.dusk.externalAPIs.apiInterfaces.models;

import lombok.Data;

@Data
public class MobileMoneyPaymentRequest {

    private String amount;
    private String currencyIso;
    private String apiKey;
    private String siteId;
    private String orderId;
    private String transactionId;
    private String description;
    private String returnUrl;
    private String notifyUrl;
    private String cancelUrl;
    private String metadata;
    private String customerId;
    private String customerFirstName;
    private String customerLastName;
    private String customerCountry;
    private String operator;
    private String lang;

}
