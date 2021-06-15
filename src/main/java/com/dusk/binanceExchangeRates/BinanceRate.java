package com.dusk.binanceExchangeRates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BinanceRate {

    @Id
    protected String id;
    protected String symbol;
    protected String eventType;
    protected Long eventTime;
    protected Long timestamp;
    protected Ticks ticks;

}
