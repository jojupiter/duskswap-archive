package com.dusk.binanceExchangeRates.models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("eurusdt")
@Data
public class EurUsdt extends BinanceRate {
    public EurUsdt(String id, String symbol, String eventType, Long eventTime, Long timestamp, Ticks ticks) {
        super(id, symbol, eventType, eventTime, timestamp, ticks);
    }

    @Override
    public String toString() {
        return "EurUsdt{" +
                "id='" + id + '\'' +
                ", symbol='" + symbol + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventTime=" + eventTime +
                ", timestamp=" + timestamp +
                ", ticks=" + ticks +
                '}';
    }
}
