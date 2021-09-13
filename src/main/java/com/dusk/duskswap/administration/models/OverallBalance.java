package com.dusk.duskswap.administration.models;

import com.dusk.duskswap.commons.models.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "overall_balance")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OverallBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deposit_balance")
    private String depositBalance; // amount of all crypto or fiat bought or deposited

    @Column(name = "withdrawal_balance")
    private String withdrawalBalance; // amount of all crypto or fiat available for sale or withdraw

    @ManyToOne
    @JoinColumn(name = "currency_id", referencedColumnName = "id")
    private Currency currency;

}
