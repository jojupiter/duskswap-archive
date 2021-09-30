package com.dusk.externalAPIs.cinetpay.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentInit {

    private String apikey;
    private String site_id;
    private String transaction_id;
    private String amount;
    private String currency; // XOF, XAF, CDF, GNF
    private String description;
    private String customer_id;
    private String customer_name;
    private String customer_surname;
    private String notify_url;
    private String return_url;
    private String channels; // ALL, MOBILE_MONEY, CREDIT_CARD
    private String lang; // fr, en
    private String metadata;

    // optional: for payments via bank
    private String customer_phone_number;
    private String customer_email;
    private String customer_address;
    private String customer_city;
    private String customer_country; // iso of the country ex : CI, BF, US, CA, FR
    private String customer_state;
    private String customer_zip_code;

}
