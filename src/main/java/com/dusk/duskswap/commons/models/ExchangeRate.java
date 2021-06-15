package com.dusk.duskswap.commons.models;

import com.dusk.shared.commons.models.Auditable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "exchange_rate")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRate extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
