package com.dusk.duskswap.commons.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "currency")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Currency extends Auditable<String>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iso")
    private String iso;

    @Column(name = "name")
    private String name;

    @Column(name = "is_supported")
    private Boolean isSupported;

}

