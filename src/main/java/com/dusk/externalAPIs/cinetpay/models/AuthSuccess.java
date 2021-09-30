package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

import java.util.Map;

@Data
public class AuthSuccess {

    private Integer code;
    private String message;
    private Map<String, String> data;

}
