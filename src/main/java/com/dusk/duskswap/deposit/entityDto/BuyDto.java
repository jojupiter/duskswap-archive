package com.dusk.duskswap.deposit.entityDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BuyDto {
    private String amount;
    private Long toCurrencyId;
    private String transactionOptIso;
}
