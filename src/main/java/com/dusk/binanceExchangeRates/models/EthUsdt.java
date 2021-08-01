package com.dusk.binanceExchangeRates.models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("ethusdt")
@Data
public class EthUsdt extends BinanceRate{
    public EthUsdt(String id, String symbol, String eventType, Long eventTime, Long timestamp, Ticks ticks) {
        super(id, symbol, eventType, eventTime, timestamp, ticks);
    }

    @Override
    public String toString() {
        return "EthUsdt{" +
                "id='" + id + '\'' +
                ", symbol='" + symbol + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventTime=" + eventTime +
                ", timestamp=" + timestamp +
                ", ticks=" + ticks +
                '}';
    }
}
