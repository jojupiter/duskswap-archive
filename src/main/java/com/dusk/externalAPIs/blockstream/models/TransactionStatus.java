package com.dusk.externalAPIs.blockstream.models;

import lombok.Data;

@Data
public class TransactionStatus {

    private Boolean confirmed;
    private Integer block_height;
    private String block_hash;
    private Long block_time;

}
