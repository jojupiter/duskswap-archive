package com.dusk.duskswap.withdrawal.models;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.commons.models.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Table(name = "withdrawal")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Withdrawal extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_address")
    private String clientAddress; // crypto address of user

    @Column(name = "amount")
    private String amount; // amount of crypto for withdrawal

    @Column(name = "dusk_fees_crypto")
    private String duskFeesCrypto; // duskswap fees in CRYPTO

    @Column(name = "network_fees")
    private String networkFees;

    @Column(name = "withdrawal_date")
    private Date withdrawalDate;

    @Column(name = "transaction_hash")
    private String transactionHash;

    @ManyToOne
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "currency_id", referencedColumnName = "id")
    private Currency currency;

    @ManyToOne
    @JoinColumn(name = "exchange_account_id", referencedColumnName = "id")
    private ExchangeAccount exchangeAccount;


}
