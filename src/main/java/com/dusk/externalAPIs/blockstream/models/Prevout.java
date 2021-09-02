package com.dusk.externalAPIs.blockstream.models;

import lombok.Data;

@Data
public class Prevout {

    private String scriptpubkey;
    private String scriptpubkey_asm;
    private String scriptpubkey_type;
    private String scriptpubkey_address;
    private Double value;

}
