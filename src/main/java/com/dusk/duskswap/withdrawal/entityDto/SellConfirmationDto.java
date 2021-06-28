package com.dusk.duskswap.withdrawal.entityDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SellConfirmationDto {
    private Long sellId;
    private String jwtToken;
}
