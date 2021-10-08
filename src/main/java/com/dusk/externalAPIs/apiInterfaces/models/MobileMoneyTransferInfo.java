package com.dusk.externalAPIs.apiInterfaces.models;

import lombok.Data;

@Data
public class MobileMoneyTransferInfo {

    private String phonePrefix;
    private String phone;
    private String firstName;
    private String lastName;
    private String email;
    private String amount;
    private String currencyIso;
    private String notifyUrl;
    private String transactionId;
    private String paymentToken;
    private String lang;
    private String status;
    private String operator;
    private String validationDate;

}
