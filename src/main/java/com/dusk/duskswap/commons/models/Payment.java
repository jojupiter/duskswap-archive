package com.dusk.duskswap.commons.models;

import lombok.Data;

@Data
public class Payment {

    private String id; // transaction hash
    private Integer receivedDate;
    private String value;
    private String fee;
    private String status;
    private String destination;

}
