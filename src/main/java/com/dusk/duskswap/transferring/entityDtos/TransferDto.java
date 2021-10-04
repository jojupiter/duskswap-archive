package com.dusk.duskswap.transferring.entityDtos;

import lombok.Data;

@Data
public class TransferDto {

    private Long recipientUserId;
    private Integer code;
    private Long currencyId;
    private String amount;

}
