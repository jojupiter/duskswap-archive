package com.dusk.duskswap.account.entityDto;

import lombok.Data;

@Data
public class CryptoBalance {

    private Long id;
    private String crypto;
    private String iso;
    private String amount;

}
