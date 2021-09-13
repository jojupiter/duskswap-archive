package com.dusk.externalAPIs.blockstream.models;

import lombok.Data;
import java.util.List;

@Data
public class Transaction {

    private String txid;
    private Integer version;
    private Integer locktime;
    private Integer size;
    private Double weight;
    private Double fee; // in sat/vb
    private List<Vin> vin;
    private List<Vout> vout;
    private TransactionStatus status;

}
