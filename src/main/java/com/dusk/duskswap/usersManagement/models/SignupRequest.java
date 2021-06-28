package com.dusk.duskswap.usersManagement.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SignupRequest{

    private String email;
    private String password;
    private List<String> roles;

}
