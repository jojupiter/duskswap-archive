package com.dusk.duskswap.commons.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "verification_code")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationCode extends Auditable<String>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "code")
    private Integer code;
    @Column(name = "valid_until")
    private Date validUntil;
    @Column(name = "purpose")
    private String purpose;
    @Column(name = "user_email")
    private String userEmail;

}
