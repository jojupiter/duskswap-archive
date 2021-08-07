package com.dusk.duskswap.commons.models;

import lombok.Data;

import java.util.List;

@Data
public class WalletTransaction {

    private List<WalletTransactionDestination> destinations;
    private Double feeRate;
    private Boolean proceedWithPayjoin;
    private Boolean proceedWithBroadcast;
    private Boolean noChange;
    private Boolean rbf;
    private List<String> selectedInputs;

}
