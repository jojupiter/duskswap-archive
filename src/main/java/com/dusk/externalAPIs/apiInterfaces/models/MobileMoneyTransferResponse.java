package com.dusk.externalAPIs.apiInterfaces.models;

import lombok.Data;

@Data
public class MobileMoneyTransferResponse {

    private String transactionId;
    private String clientTransactionId;
    private String receiverPhone;
    private String operator;
    private String validationDate;
    private String status;
    private String amount;
    private String transferValid;
    private String comment;

}
