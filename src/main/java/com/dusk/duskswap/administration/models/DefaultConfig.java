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

    // exchange rates
    @Column(name = "usd_xaf_buy")
    private String usdToXafBuy; // exchange rate for buying purpose

    @Column(name = "usd_xaf_sell")
    private String usdToXafSell; // exchange rate for selling purpose

    // mobile money apis used
    @OneToOne
    @JoinColumn(name = "om_api_used", referencedColumnName = "id")
    private PaymentAPI omAPIUsed; // Orange money API used (payment and transfer)

    @OneToOne
    @JoinColumn(name = "momo_api_used", referencedColumnName = "id")
    private PaymentAPI momoAPIUsed; // Orange money API used  (payment and transfer)


}
