package com.dusk.binanceExchangeRates.models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("ethusd")
@Data
public class EthUsd extends BinanceRate{
    public EthUsd(String id, String symbol, String eventType, Long eventTime, Long timestamp, Ticks ticks) {
        super(id, symbol, eventType, eventTime, timestamp, ticks);
    }

    @Override
    public String toString() {
        return getClass().getName() + " (id = " + id +
                ", symbol = " + symbol +
                ", eventType = " + eventType +
                ", eventTime = " + eventTime +
                ", timestamp =  " + timestamp +
                ", ticks = " + ticks +
                ")";
    }
    
}
