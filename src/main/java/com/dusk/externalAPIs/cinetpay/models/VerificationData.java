package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

@Data
public class VerificationData {

    private String operator_id;
    private String payment_method;
    private String metadata;
    private String payment_date;
    private String phone_number;
    private String phone_prefix;

}
