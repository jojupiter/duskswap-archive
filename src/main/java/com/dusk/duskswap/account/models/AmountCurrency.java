package com.dusk.duskswap.account.models;

import com.dusk.duskswap.commons.models.Auditable;
import com.dusk.duskswap.commons.models.Currency;
import lombok.*;

import javax.persistence.*;

@Table(name = "amount_currency")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AmountCurrency extends Auditable<String> { // Entity association from exchange and currency

    @EmbeddedId
    private AmountCurrencyKey id;

    @ManyToOne
    @MapsId("exchange_account_id")
    @JoinColumn(name = "exchange_account_id")
    private ExchangeAccount exchangeAccount;

    @ManyToOne
    @MapsId("currency_id")
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @Column(name = "amount")
    private String amount;

    @Override
    public String toString() {
        return "AmountCurrency{" +
                "id=" + id +
                ", exchangeAccountId=" + exchangeAccount.getId() +
                ", currency=" + currency +
                ", amount='" + amount + '\'' +
                '}';
    }

}
