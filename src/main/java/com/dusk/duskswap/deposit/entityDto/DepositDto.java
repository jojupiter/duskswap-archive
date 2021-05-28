package com.dusk.duskswap.deposit.entityDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepositDto {

    private String fromAddress;
    private Double amount;
    private String currency;
    private String jwtToken;

}
