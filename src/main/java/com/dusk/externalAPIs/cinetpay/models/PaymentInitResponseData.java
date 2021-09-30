package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

@Data
public class PaymentInitResponseData {

    private String payment_token;
    private String payment_url;

}
