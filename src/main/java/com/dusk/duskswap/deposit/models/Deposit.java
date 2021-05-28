package com.dusk.duskswap.deposit.models;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.commons.models.ExchangeRate;
import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.shared.commons.models.Auditable;
import com.dusk.shared.commons.models.Currency;
import com.dusk.shared.commons.models.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "deposit")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Deposit extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_address")
    private String fromAddress;

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
    private ExchangeRate exchangeRate;

}
