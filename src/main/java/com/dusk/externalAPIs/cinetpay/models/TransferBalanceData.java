package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

@Data
public class TransferBalanceData {

    private Double amount;
    private Integer inUsing;
    private Double available;

}
