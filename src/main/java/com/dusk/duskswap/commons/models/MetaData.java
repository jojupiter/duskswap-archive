package com.dusk.duskswap.commons.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetaData {

    private String orderId;
    private String buyerName;
    private String buyerEmail;
    private String buyerCountry;
    private String buyerZip;
    private String buyerState;
    private String buyerCity;
    private String buyerAddress1;
    private String buyerAddress2;
    private String buyerPhone;
    private String itemDesc;
    private String itemCode;
    private String physical;
    private String posData;
    private Double taxIncluded;

}
