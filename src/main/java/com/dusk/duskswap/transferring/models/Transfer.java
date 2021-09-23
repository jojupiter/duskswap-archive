package com.dusk.duskswap.transferring.models;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.commons.models.Auditable;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "transfer")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transfer extends Auditable<String> { // transfer uses the same currency

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_account", referencedColumnName = "id")
    private ExchangeAccount fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_account", referencedColumnName = "id")
    private ExchangeAccount toAccount;

    @ManyToOne
    @JoinColumn(name = "currency_id", referencedColumnName = "id")
    private Currency currency;

    @Column(name = "amount")
    private String amount;

    @Column(name = "fees")
    private String fees; // transfer fees for that currency

    @ManyToOne
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private Status status;

}
