package com.dusk.duskswap.usersManagement.models;

import com.dusk.duskswap.commons.models.Auditable;
import com.dusk.duskswap.commons.models.Level;
import com.dusk.duskswap.commons.models.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "person")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    @JsonProperty("username")
    @NotNull
    private String username;

    @Column(name = "password")
    @NotNull
    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
    private String encryptedPassword;

    @Column(name = "tel")
    @JsonProperty("tel")
    private String tel;

    @Column(name = "email")
    @JsonProperty("email")
    @Email(message = "Email should be valid!")
    @NotNull
    private String  email;

    @Column(name = "firstname")
    @JsonProperty("firstname")
    private String firstName;

    @Column(name = "lastname")
    @JsonProperty("lastname")
    private String lastName;

    @Column(name = "last_login")
    @JsonProperty("last_login")
    @JsonFormat(pattern="dd/MM/yyyy HH:mm:ss")
    private Date lastLogin;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "person_role",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles  = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private Status status;

    @OneToOne
    @JoinColumn(name = "level_id", referencedColumnName = "id")
    private Level level;

}
