package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

@Data
public class PaymentInitSuccessData {

    private String payment_token;
    private String payment_url;

}
