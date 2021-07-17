package com.dusk.duskswap.exchange.entityDto;

import lombok.Data;

@Data
public class ExchangeDto {

    private String fromAmount;
    private String toAmount;
    private Long fromCurrencyId;
    private Long toCurrencyId;

}
