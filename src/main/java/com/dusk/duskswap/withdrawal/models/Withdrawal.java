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

@Table(name = "withdrawal")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Withdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "to_address")
    private String toAddress;

    @Column(name = "amount")
    private Double amount;

    @ManyToOne
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "currency_id", referencedColumnName = "id")
    private Currency currency;

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
