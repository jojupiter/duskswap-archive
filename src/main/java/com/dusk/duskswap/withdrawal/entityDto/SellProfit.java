package com.dusk.duskswap.withdrawal.entityDto;

import com.dusk.duskswap.commons.models.Currency;
import lombok.Data;

@Data
public class SellProfit {
    private Currency currency;
    private String sum;
}
