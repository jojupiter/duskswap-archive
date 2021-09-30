package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

@Data
public class Contact {

    private String prefix;
    private String phone;
    private String name;
    private String surname;
    private String email;
    private Double amount;
    private String client_transaction_id;
    private String notify_url;
    private Integer code;
    private String status;
    private String treatment_status;
    private String lot;

}
