package com.dusk.duskswap.withdrawal.models;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.commons.models.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Table(name = "sell")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Sell extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_amount_crypto")
    private String totalAmountCrypto; // the total amount of crypto that the user want to sell (the amount he enters) (it's in this amount we take fees)

    @Column(name = "amount_received")
    private String amountReceived; // the exact amount converted in FIAT the user should receive when selling

    @Column(name = "dusk_fees_crypto")
    private String duskFeesCrypto; // duskswap fees in CRYPTO

    @Column(name = "dusk_fees")
    private String duskFees; // duskswap fees in FIAT (sometimes with extra decimal got when for example when amount to send = 2340.42, we add the extra 0.42 to duskFees and send effectively 2340)

    @Column(name = "tel")
    private String tel;

    @Column(name = "crypto_price_usdt")
    private String cryptoPriceInUsdt;

    @Column(name = "usd_to_fiat")
    private String usdToFiat;

    @Column(name = "sell_date")
    private Date sellDate;

    @Column(name = "transaction_id")
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "currency_id", referencedColumnName = "id")
    private Currency currency;

    @ManyToOne
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "exchange_account_id", referencedColumnName = "id")
    private ExchangeAccount exchangeAccount;

    @ManyToOne
    @JoinColumn(name = "transaction_option_id", referencedColumnName = "id")
    private TransactionOption transactionOption;

}
