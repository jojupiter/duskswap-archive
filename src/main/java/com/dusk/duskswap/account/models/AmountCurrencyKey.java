package com.dusk.duskswap.account.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmountCurrencyKey implements Serializable {

    @Column(name = "exchange_account_id")
    private Long exchangeAccountId;

    @Column(name = "currency_id")
    private Long currencyId;

}
