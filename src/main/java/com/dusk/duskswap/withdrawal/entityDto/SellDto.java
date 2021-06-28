package com.dusk.duskswap.withdrawal.entityDto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SellDto {

    private Long fromCurrencyId;
    private String amount;
    private String jwtToken;
    private String tel;
    private Long transactionOptId;
    private Long toCurrencyId;

}
