package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

@Data
public class Verification {

    private String code;
    private String message;
    private String api_response_id;
    private VerificationData data;

}
