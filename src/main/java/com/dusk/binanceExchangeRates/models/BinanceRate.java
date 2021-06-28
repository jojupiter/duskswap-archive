package com.dusk.binanceExchangeRates.models;

import lombok.*;

import javax.persistence.Id;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BinanceRate {

    @Id
    protected String id;
    protected String symbol;
    protected String eventType;
    protected Long eventTime;
    protected Long timestamp;
    protected Ticks ticks;

}
