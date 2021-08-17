package com.dusk.duskswap.commons.models;

import lombok.Data;

@Data
public class WalletBalance {

    private String balance;
    private String unconfirmedBalance;
    private String confirmedBalance;
    private String label;

}
