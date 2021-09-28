package com.dusk.duskswap.transferring.entityDtos;

import lombok.Data;

@Data
public class TransferResponse {

    private String recipientEmail;
    private String amount;
    private String fees;

}
