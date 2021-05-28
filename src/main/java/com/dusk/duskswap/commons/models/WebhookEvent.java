package com.dusk.duskswap.commons.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebhookEvent {

    private Long id;
    private String deliveryId;
    private String webhookId;
    private String originalDeliveryId;
    private Boolean isRedelivery;
    private String type;
    private Integer timestamp;
    private String storeId;
    private String invoiceId;

}
