package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

import java.util.List;

@Data
public class TransferInfo {

    private Integer code;
    private String message;
    private List<TransferInfoData> data;

}
