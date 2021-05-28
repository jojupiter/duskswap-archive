package com.dusk.shared.newslettersManagement.models;

import com.dusk.shared.commons.models.Auditable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "newsletter_subscriber")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsLetterSubScriber extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    @NotBlank
    private String email;

    @Column(name = "accept")
    @NotBlank
    private Boolean accept;

}
