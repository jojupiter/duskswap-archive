package com.dusk.duskswap.commons.entityDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OMPaymentResponse {

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("pay_token")
    private String payToken;

    @JsonProperty("payment_url")
    private String paymentUrl;

    @JsonProperty("notif_token")
    private String notifToken;

}
