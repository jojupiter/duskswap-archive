package com.dusk.binanceExchangeRates.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Ticks {

    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private Integer trades;
    private String interval;
    private Boolean isFinal;
    private String quoteVolume;
    private String buyVolume;
    private String quoteBuyVolume;

}
