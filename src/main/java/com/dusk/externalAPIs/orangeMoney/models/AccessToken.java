package com.dusk.externalAPIs.orangeMoney.models;

import lombok.Data;

@Data
public class AccessToken {

    private String token_type;
    private String access_token;
    private Integer expires_in;

}
