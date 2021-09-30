package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

@Data
public class PaymentInitResponse {

    private String code;
    private String message;
    private String description;
    private PaymentInitResponseData data;
    private String api_response_id;

}
