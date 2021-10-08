package com.dusk.externalAPIs.apiInterfaces.models;

import lombok.Data;

@Data
public class AuthResponse {

    private String token;
    private String tokenType;
    private String expirationTime;

}
