package com.dusk.duskswap.withdrawal.models;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.commons.models.Conversion;
import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "sell")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Sell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount")
    private String amount;

    @Column(name = "tel")
    private String tel;

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

    @ManyToOne
    @JoinColumn(name = "exchange_rate_id", referencedColumnName = "id")
    private Conversion conversion;

}
