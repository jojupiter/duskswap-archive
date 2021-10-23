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
    @Column(name = "om_payment_api_used")
    private String omPaymentAPIUsed; // Orange money API used for payment (if it's cinetpay or even Orange API).

    @Column(name = "om_transfer_api_used")
    private String omTransferAPIUsed; // Orange money API used for transfer

    @Column(name = "momo_payment_api_used")
    private String momoPaymentAPIUsed; // Mtn Mobile money API used for payment (if it's cinetpay or even Mtn API)

    @Column(name = "momo_transfer_api_used")
    private String momoTransferAPIUsed; // Mtn Mobile money API used for transfer (if it's cinetpay or even Mtn API)

    // mobile money api fees

    @Column(name = "cinetpay_payment_fees")
    private String cinetpayPaymentFees; // Cinetpay API payment fees

    @Column(name = "cinetpay_transfer_fees")
    private String cinetpayTransferFees; // Cinetpay API transfer fees

    @Column(name = "om_payment_fees")
    private String omPaymentFees; // Orange Money API payment fees

    @Column(name = "om_transfer_fees")
    private String omTransferFees; // Orange Money API transfer fees

    @Column(name = "momo_payment_fees")
    private String momoPaymentFees; // Orange Money API payment fees

    @Column(name = "momo_transfer_fees")
    private String momoTransferFees; // Orange Money API transfer fees

}
