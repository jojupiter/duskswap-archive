package com.dusk.duskswap.commons.models;

import lombok.Data;

import java.util.List;

@Data
public class InvoicePayment {

    private String paymentMethod;
    private String destination;
    private String paymentLink;
    private String rate;
    private String paymentMethodPaid;
    private String totalPaid;
    private String due;
    private String amount;
    private String networkFee;
    private Boolean activated;
    private List<Payment> payments;

}
