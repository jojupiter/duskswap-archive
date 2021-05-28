package com.dusk.shared.usersManagement.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;


@Entity
@Table(name = "role")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    @NotBlank
    private String name;

    @Column(name = "created_date")
    @JsonFormat(pattern="dd/MM/yyyy HH:mm:ss")
    @NotNull
    @CreatedDate
    private Date createdDate;

    @Column(name = "last_update")
    @JsonFormat(pattern="dd/MM/yyyy HH:mm:ss")
    private Date lastUpdate;

}
