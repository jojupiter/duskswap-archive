package com.dusk.externalAPIs.orangeMoney.models;

import lombok.Data;

@Data
public class PaymentResponse {

    private Integer status;
    private String message;
    private String pay_token;
    private String payment_url;
    private String notif_token;

}
