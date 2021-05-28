package com.dusk.duskswap.withdrawal.entityDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawalDto {

    private String toAddress;
    private Double amount;
    private String currency;
    private String jwtToken;

}
