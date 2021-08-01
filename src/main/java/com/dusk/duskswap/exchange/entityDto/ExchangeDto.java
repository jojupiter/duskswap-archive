package com.dusk.duskswap.exchange.entityDto;

import lombok.Data;

@Data
public class ExchangeDto {

    private String fromAmount;
    private Long fromCurrencyId;
    private Long toCurrencyId;

}
