package com.dusk.binanceExchangeRates.factories;

import com.dusk.binanceExchangeRates.models.BtcUsd;
import com.dusk.binanceExchangeRates.models.EthUsd;

public class BinanceRateFactory {

    public Class<?> getBinanceClassFromName(String name) {
        if(name.equals("btc") || name.equals("BTC")|| name.equals("btcusd") || name.equals("BTCUSD"))
            return BtcUsd.class;
        if(name.equals("eth") || name.equals("ETH") || name.equals("ethusd") || name.equals("ETHUSD"))
            return EthUsd.class;
        return null;
    }

}
