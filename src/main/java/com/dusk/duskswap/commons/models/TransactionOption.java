package com.dusk.duskswap.commons.models;

import com.dusk.shared.commons.models.Auditable;
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

}
