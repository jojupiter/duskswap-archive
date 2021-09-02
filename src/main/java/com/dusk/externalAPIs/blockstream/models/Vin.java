package com.dusk.externalAPIs.blockstream.models;

import lombok.Data;

import java.util.List;

@Data
public class Vin {

    private String txid;
    private Integer vout;
    private String scriptsig;
    private String scriptsig_asm;
    private List<String> witness;
    private Boolean is_coinbase;
    private Long sequence;
    private String inner_redeemscript_asm;
    private Prevout prevout;

}
