package com.dusk.duskswap.administration.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "payment_api")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentAPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "api_full_name")
    private String apiFullName;
    @Column(name = "api_iso")
    private String apiIso;

    @Column(name = "payment_fees")
    private String paymentFees; // fees in %
    @Column(name = "transfer_fees")
    private String transferFees;

}
