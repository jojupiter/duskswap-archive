package com.dusk.duskswap.commons.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "transaction_option")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionOption extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "iso")
    private String iso;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "is_supported")
    private Boolean isSupported;

}
