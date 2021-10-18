package com.dusk.duskswap.administration.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "default_config")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefaultConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usd_xaf_buy")
    private String usdToXafBuy; // exchange rate for buying purpose

    @Column(name = "usd_xaf_sell")
    private String usdToXafSell; // exchange rate for selling purpose


}
