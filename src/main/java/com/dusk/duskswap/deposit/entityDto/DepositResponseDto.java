package com.dusk.duskswap.deposit.entityDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DepositResponseDto {
    private Long depositId;
    private String invoiceSourceCode;
}
