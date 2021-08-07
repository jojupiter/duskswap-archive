package com.dusk.duskswap.withdrawal.entityDto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SellDto {

    private Long fromCurrencyId;
    private String amount; // the amount the user desires to sell
    private Integer code; // verification code
    private String tel;
    private Long transactionOptId; // OM, MOMO, ....

}
