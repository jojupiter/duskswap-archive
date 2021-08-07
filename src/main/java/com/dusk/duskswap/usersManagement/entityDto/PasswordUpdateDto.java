package com.dusk.duskswap.usersManagement.entityDto;

import lombok.Data;

@Data
public class PasswordUpdateDto {
    private String code;
    private String newPassword;
    private String email;
}
