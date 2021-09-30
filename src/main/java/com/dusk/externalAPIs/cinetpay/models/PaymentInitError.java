package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

@Data
public class PaymentInitError {

    private String code;
    private String message;
    private String description;
    private String api_response_id;

}
