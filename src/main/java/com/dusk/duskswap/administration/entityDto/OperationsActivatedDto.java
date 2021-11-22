package com.dusk.duskswap.administration.entityDto;

import lombok.Data;

@Data
public class OperationsActivatedDto {

    private Long currencyId;
    private Boolean isBuyActivated;
    private Boolean isDepositActivated;
    private Boolean isExchangeActivated;
    private Boolean isTransferActivated;
    private Boolean isSellActivated;
    private Boolean isWithdrawalActivated;

}
