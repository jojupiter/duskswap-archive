package com.dusk.externalAPIs.apiInterfaces.models;

import lombok.Data;

@Data
public class TransactionInfos {

    private Double fees;
    private Boolean isConfirmed;
    private Integer nConfirmations; // number of confirmations
    private String inAddress;
    private String outAddress;
    private String cryptoIso;

}
