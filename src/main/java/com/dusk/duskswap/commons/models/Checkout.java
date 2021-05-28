package com.dusk.duskswap.commons.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Checkout {

    private String speedPolicy;
    private List<String> paymentMethods;
    private Integer expirationMinutes;
    private Integer monitoringMinutes;
    private Double paymentTolerance;
    private String redirectURL;
    private Boolean redirectAutomatically;
    private String defaultLanguage;

}
