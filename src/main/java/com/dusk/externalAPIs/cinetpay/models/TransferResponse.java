package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

import java.util.List;

@Data
public class TransferResponse {

    private Integer code;
    private String message;
    private String description;
    private List<List<Contact>> data;

}
