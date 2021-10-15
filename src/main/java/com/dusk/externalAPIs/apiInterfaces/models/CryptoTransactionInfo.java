package com.dusk.externalAPIs.apiInterfaces.models;

import lombok.Data;

@Data
public class CryptoTransactionInfo {

    private Double fees;
    private Long nConfirmations; // number of confirmations
    private String inAddress;
    private String outAddress;
    private String cryptoIso;

}
