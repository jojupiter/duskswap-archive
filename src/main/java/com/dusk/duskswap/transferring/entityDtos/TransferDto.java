package com.dusk.duskswap.transferring.entityDtos;

import lombok.Data;

@Data
public class TransferDto {

    private String usernameOrEmail; // recipient
    private Long currencyId;
    private String amount;

}
