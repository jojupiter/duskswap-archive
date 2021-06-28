package com.dusk.duskswap.commons.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "exchange_rate")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Conversion extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_currency")
    private String fromCurrency;

    @Column(name = "to_currency")
    private String toCurrency;

    @Column(name = "market_price")
    private String marketPrice;

    @Column(name = "dusk_price")
    private String duskPrice;

}
