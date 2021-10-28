package com.dusk.duskswap.administration.entityDto;

import lombok.Data;

@Data
public class DefaultConfigDto {

    private String usdToXafBuy;
    private String usdToXafSell;
    private String omPaymentApiUsedIso;
    private String momoPaymentApiUsedIso;

}
