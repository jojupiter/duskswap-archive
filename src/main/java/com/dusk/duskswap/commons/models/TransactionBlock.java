package com.dusk.duskswap.commons.models;

import lombok.Data;

@Data
public class TransactionBlock {

    private String transactionHash;
    private String comment;
    private String amount;
    private Object labels;
    private Integer blockHeight;
    private String blockHash;
    private String confirmations;
    private Integer timestamp;
    private String status;

}
