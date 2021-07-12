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

    @Column(name = "type")
    private String type; // percentage or fix

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

    @Column(name = "withdrawal_fees")
    private String withdrawalFees;
    @Column(name = "withdrawal_min")
    private String withdrawalMin;
    @Column(name = "withdrawal_max")
    private String withdrawalMax;

    @Column(name = "sell_fees")
    private String sellFees;
    @Column(name = "sell_min")
    private String sellMin;
    @Column(name = "sell_max")
    private String sellMax;

    @Column(name = "exchange_fees")
    private String exchangeFees; // in %
    @Column(name = "exchange_min")
    private String exchangeMin;
    @Column(name = "exchange_max")
    private String exchangeMax;

}
