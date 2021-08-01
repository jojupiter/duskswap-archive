package com.dusk.binanceExchangeRates.models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("btcusdt")
@Data
public class BtcUsdt extends BinanceRate{

    public BtcUsdt(String id, String symbol, String eventType, Long eventTime, Long timestamp, Ticks ticks) {
        super(id, symbol, eventType, eventTime, timestamp, ticks);
    }

    @Override
    public String toString() {
        return "BtcUsdt{" +
                "id='" + id + '\'' +
                ", symbol='" + symbol + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventTime=" + eventTime +
                ", timestamp=" + timestamp +
                ", ticks=" + ticks +
                '}';
    }
}
