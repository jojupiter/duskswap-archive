package com.dusk.externalAPIs.apiInterfaces.models;

import lombok.Data;

@Data
public class AuthRequest {

    private String apiKey;
    private String password;
    private String consumerKey;
    private String clientId;
    private String clientSecret;

}
