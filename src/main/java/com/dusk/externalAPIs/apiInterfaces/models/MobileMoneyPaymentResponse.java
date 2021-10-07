package com.dusk.externalAPIs.apiInterfaces.models;

import lombok.Data;

@Data
public class MobileMoneyPaymentResponse {

    private String paymentToken;
    private String notifyToken;
    private String paymentUrl;
    private String message;
    private String description;
    private String status;
    private String apiFees;

}
