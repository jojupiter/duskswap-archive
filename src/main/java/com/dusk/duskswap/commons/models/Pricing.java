package com.dusk.duskswap.commons.models;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "pricing")
@Data
public class Pricing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "level_id", referencedColumnName = "id")
    private Level level;

    @ManyToOne
    @JoinColumn(name = "currency_id", referencedColumnName = "id")
    private Currency currency;

    @Column(name = "type_buy")
    private String typeBuy; // percentage or fix
    @Column(name = "buy_fees")
    private String buyFees;
    @Column(name = "buy_max")
    private String buyMax;
    @Column(name = "buy_min")
    private String buyMin;

    @Column(name = "deposit_min")
    private String depositMin;
    @Column(name = "deposit_max")
    private String depositMax;

    @Column(name = "type_withdrawal")
    private String typeWithdrawal; // percentage or fix
    @Column(name = "withdrawal_fees")
    private String withdrawalFees;
    @Column(name = "withdrawal_min")
    private String withdrawalMin;
    @Column(name = "withdrawal_max")
    private String withdrawalMax;

    @Column(name = "type_sell")
    private String typeSell; // percentage or fix
    @Column(name = "sell_fees")
    private String sellFees;
    @Column(name = "sell_min")
    private String sellMin;
    @Column(name = "sell_max")
    private String sellMax;

    @Column(name = "type_exchange")
    private String typeExchange; // percentage or fix
    @Column(name = "exchange_fees")
    private String exchangeFees; // in %
    @Column(name = "exchange_min")
    private String exchangeMin;
    @Column(name = "exchange_max")
    private String exchangeMax;

    @Column(name = "type_transfer")
    private String typeTransfer; // percentage or fix
    @Column(name = "transferFees")
    private String transferFees;
    @Column(name = "transferMin")
    private String transferMin;
    @Column(name = "transferMax")
    private String transferMax;

}
