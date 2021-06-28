package com.dusk.duskswap.withdrawal.entityDto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SellPriceDto {

    private String fromCurrency;
    private String amount;
    private String usdPrice;

}
