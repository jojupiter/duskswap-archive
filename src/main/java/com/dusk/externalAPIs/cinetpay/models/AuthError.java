package com.dusk.externalAPIs.cinetpay.models;

import lombok.Data;

import java.util.List;

@Data
public class AuthError {

    private Integer code;
    private String message;
    private String description;
    private List<Object> data;

}
