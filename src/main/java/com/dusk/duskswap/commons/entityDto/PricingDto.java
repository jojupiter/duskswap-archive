package com.dusk.duskswap.commons.entityDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PricingDto {

    private Long levelId;
    private Long currencyId;
    private String type; // percentage or fix

    private String buyFees;
    private String buyMax;
    private String buyMin;

    private String depositMin;
    private String depositMax;

    private String withdrawalFees;
    private String withdrawalMin;
    private String withdrawalMax;

    private String sellFees;
    private String sellMin;
    private String sellMax;

    private String exchangeFees; // in %
    private String exchangeMin;
    private String exchangeMax;

}
