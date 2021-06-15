package com.dusk.duskswap.deposit.entityDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepositDto {

    private String amount;
    private Long currencyId;
    private String jwtToken;
    private Long transactionOptId;

}
