package com.dusk.externalAPIs.orangeMoney.models;

import lombok.Data;

@Data
public class PaymentRequest {

    private String merchant_key;
    private String currency;
    private String order_id;
    private Long amount;
    private String return_url;
    private String cancel_url;
    private String notif_url;
    private String lang;
    private String reference;

}
