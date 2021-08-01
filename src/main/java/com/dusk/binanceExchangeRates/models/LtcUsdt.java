package com.dusk.binanceExchangeRates.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Data
@Document("ltcusdt")
public class LtcUsdt extends BinanceRate {

    public LtcUsdt(String id, String symbol, String eventType, Long eventTime, Long timestamp, Ticks ticks) {
        super(id, symbol, eventType, eventTime, timestamp, ticks);
    }

    @Override
    public String toString() {
        return "LtcUsdt{" +
                "id='" + id + '\'' +
                ", symbol='" + symbol + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventTime=" + eventTime +
                ", timestamp=" + timestamp +
                ", ticks=" + ticks +
                '}';
    }
}
