package com.dusk.duskswap.commons.models;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Invoice {

    private String id;
    private String checkoutLink;
    private String status;
    private String additionalStatus;
    private Integer monitoringExpiration;
    private Integer expirationTime;
    private Integer createdTime;
    private String amount;
    private String currency;
    private MetaData metadata;
    private Checkout checkout;

}
