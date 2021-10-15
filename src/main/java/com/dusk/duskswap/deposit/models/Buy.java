package com.dusk.duskswap.deposit.models;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.commons.models.Auditable;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Status;
import com.dusk.duskswap.commons.models.TransactionOption;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Table(name = "buy")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Buy extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_amount")
    private String totalAmount; // total amount of money the user enters in FIAT

    @Column(name = "amount_crypto")
    private String amountCrypto; // the amount of crypto that will be allocated to the user once the buy is confirmed

    @Column(name = "dusk_fees")
    private String duskFees; // duskswap fees in FIAT (including mobile money | other fees)

    @Column(name = "dusk_fees_crypto")
    private String duskFeesCrypto; // duskswap fees in CRYPTO (equivalent of duskFees in crypto)

    @Column(name = "api_fees")
    private String apiFees; // OM, MOMO, or even banking API if we have in FIAT

    @Column(name = "usdt_to_fiat")
    private String usdtToFiat;

    @Column(name = "pay_token")
    private String payToken;

    @Column(name = "notif_token")
    private String notifToken;

    @Column(name = "buy_date")
    private Date buyDate;

    @Column(name = "transaction_id")
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "exchange_account_id", referencedColumnName = "id")
    private ExchangeAccount exchangeAccount;

    @ManyToOne
    @JoinColumn(name = "to_currency_id", referencedColumnName = "id")
    private Currency toCurrency;

    @ManyToOne
    @JoinColumn(name = "transaction_option_id", referencedColumnName = "id")
    private TransactionOption transactionOption;

}
