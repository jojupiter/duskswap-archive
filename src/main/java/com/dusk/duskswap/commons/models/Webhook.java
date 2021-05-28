package com.dusk.duskswap.commons.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Webhook {

    private String id;
    private Boolean enabled;
    private Boolean automaticRedelivery;
    private String url;
    private AuthorizedEvents authorizedEvents;
    private String secret;

}
