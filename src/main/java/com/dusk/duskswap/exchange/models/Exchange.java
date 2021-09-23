package com.dusk.duskswap.exchange.models;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.commons.models.Auditable;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Status;
import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "exchange")
@Entity
public class Exchange extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_amount")
    private String fromAmount;

    @Column(name = "to_amount")
    private String toAmount;

    @Column(name = "dusk_fees")
    private String duskFees; // unit: from currency (fees are taken on the initial input currency, so the fromCurrency)

    @Column(name = "from_currency_price_usdt")
    private String fromCurrencyPriceInUsdt; // used to keep history price

    @Column(name = "to_currency_price_usdt")
    private String toCurrencyPriceInUsdt; // used to keep history price

    @ManyToOne
    @JoinColumn(name = "from_currency_id", referencedColumnName = "id")
    private Currency fromCurrency;

    @ManyToOne
    @JoinColumn(name = "to_currency_id", referencedColumnName = "id")
    private Currency toCurrency;

    @ManyToOne
    @JoinColumn(name = "exchange_account_id", referencedColumnName = "id")
    private ExchangeAccount exchangeAccount;

    @ManyToOne
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private Status status;

}
