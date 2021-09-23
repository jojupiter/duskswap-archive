package com.dusk.duskswap.deposit.models;

import com.dusk.duskswap.commons.models.Auditable;
import com.dusk.duskswap.commons.models.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;

@Table(name = "deposit_hash")
@Entity
@Data
public class DepositHash extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="transaction_hash")
    private String transactionHash;

    @Column(name = "to_deposit_address")
    private String toDepositAddress;

    @Column(name = "from_deposit_address")
    private String fromDepositAddress;

    @Column(name = "amount")
    private String amount;

    @Column(name = "is_valid")
    private Boolean isValid; // a deposit hash is valid until we have take it in to consideration when funding the user's account. This is used to avoid cases where we could increase the user's account multiple times for the same deposit hash

    @ManyToOne
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private Status status;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne
    @JoinColumn(name = "deposit_id", referencedColumnName = "id")
    private Deposit deposit;

}
