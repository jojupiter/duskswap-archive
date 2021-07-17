package com.dusk.duskswap.commons.entityDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OMTokenBearer {

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private String expiresIn;

}
