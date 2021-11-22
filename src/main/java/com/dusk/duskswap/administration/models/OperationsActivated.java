package com.dusk.duskswap.administration.models;

import com.dusk.duskswap.commons.models.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "operations_activated")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationsActivated { // this table is used to activate or deactivate our operations if needed (buy, sell, ...)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_buy_activated")
    private Boolean isBuyActivated;

    @Column(name = "is_deposit_activated")
    private Boolean isDepositActivated;

    @Column(name = "is_exchange_activated")
    private Boolean isExchangeActivated;

    @Column(name = "is_transfer_activated")
    private Boolean isTransferActivated;

    @Column(name = "is_sell_activated")
    private Boolean isSellActivated;

    @Column(name = "is_withdrawal_activated")
    private Boolean isWithdrawalActivated;

    @ManyToOne
    @JoinColumn(name = "currency_id", referencedColumnName = "id")
    private Currency currency;

}
