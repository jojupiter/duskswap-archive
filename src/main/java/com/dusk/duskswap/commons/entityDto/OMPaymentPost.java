package com.dusk.duskswap.commons.entityDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OMPaymentPost {

    @JsonProperty("merchant_key")
    private String merchantKey;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("lang")
    private String lang;

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("cancel_url")
    private String cancelUrl;

    @JsonProperty("notif_url")
    private String notifUrl;

}
