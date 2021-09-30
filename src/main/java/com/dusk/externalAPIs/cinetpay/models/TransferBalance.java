package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

@Data
public class TransferBalance {

    private Integer code;
    private String message;
    private TransferBalanceData data;

}
