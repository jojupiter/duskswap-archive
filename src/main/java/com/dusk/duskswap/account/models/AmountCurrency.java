package com.dusk.duskswap.account.models;

import com.dusk.shared.commons.models.Auditable;
import com.dusk.shared.commons.models.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "amount_currency")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmountCurrency extends Auditable<String> { // Entity association from exchange and currency

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private ExchangeAccount exchangeAccount;

    @ManyToOne
    @JoinColumn(name = "currency_id", referencedColumnName = "id")
    private Currency currency;

    @Column(name = "amount")
    private Double amount;

}
