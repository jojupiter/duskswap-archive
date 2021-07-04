package com.dusk.duskswap.withdrawal.entityDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawalDto {

    private String toAddress;
    private String amount;
    private Long currencyId;
    //private String jwtToken;
    private Integer code;

}
