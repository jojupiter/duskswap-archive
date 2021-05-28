package com.dusk.duskswap.account.models;

import com.dusk.shared.commons.models.Auditable;
import com.dusk.shared.commons.models.Currency;
import com.dusk.shared.usersManagement.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Table(name = "exchange_account")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeAccount extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "exchange_account")
    private Set<AmountCurrency> amountCurrencies;

}
