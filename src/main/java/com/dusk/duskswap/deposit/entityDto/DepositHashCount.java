package com.dusk.duskswap.deposit.entityDto;

import com.dusk.duskswap.deposit.models.Deposit;
import lombok.Data;

@Data
public class DepositHashCount {
    private Deposit deposit;
    private Long totalHashCount;
}
