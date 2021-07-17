package com.dusk.duskswap.commons.entityDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OMNotificationBody {

    @JsonProperty("status")
    private String status;

    @JsonProperty("notif_token")
    private String notifToken;

    @JsonProperty("txnid")
    private String txnid;

}
