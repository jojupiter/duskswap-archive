package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

@Data
public class PaymentInitSuccess {

    private String code;
    private String message;
    private String description;
    private PaymentInitSuccessData data;
    private String api_response_id;

}
