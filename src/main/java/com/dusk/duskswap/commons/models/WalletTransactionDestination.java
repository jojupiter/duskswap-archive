package com.dusk.duskswap.commons.models;

import lombok.Data;

@Data
public class WalletTransactionDestination {

    private String destination;
    private String amount;
    private Boolean subtractFromAmount;

}
