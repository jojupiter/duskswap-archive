package com.dusk.binanceExchangeRates.factories;

import com.dusk.binanceExchangeRates.models.BtcUsdt;
import com.dusk.binanceExchangeRates.models.EthUsdt;
import com.dusk.binanceExchangeRates.models.EurUsdt;
import com.dusk.binanceExchangeRates.models.LtcUsdt;

public class BinanceRateFactory {

    public Class<?> getBinanceClassFromName(String name) {
        if(name.equals("btc") || name.equals("BTC")|| name.equals("btcusdt") || name.equals("BTCUSDT") ||
                name.equals("btcusd") || name.equals("BTCUSD") || name.equals("BITCOIN") || name.equals("bitcoin"))
            return BtcUsdt.class;
        if(name.equals("eth") || name.equals("ETH") || name.equals("ethusdt") || name.equals("ETHUSDT") ||
                name.equals("ethusd") || name.equals("ETHUSD") || name.equals("ETHEREUM") || name.equals("ethereum"))
            return EthUsdt.class;
        if(name.equals("eur") || name.equals("EUR") || name.equals("eurusd") || name.equals("EURUSD") ||
           name.equals("eurusdt") || name.equals("EURUSDT") || name.equals("EURO") || name.equals("euro"))
            return EurUsdt.class;
        if(name.equals("ltc") || name.equals("LTC") || name.equals("ltcusdt") || name.equals("LTCUSDT") ||
           name.equals("ltcusd") || name.equals("LTCUSD") || name.equals("LITECOIN") || name.equals("litecoin"))
            return LtcUsdt.class;
        if(name.equals("shib") || name.equals("SHIB") || name.equals("shibusdt") || name.equals("SHIBUSDT") ||
                name.equals("shibusd") || name.equals("SHIBUSD") || name.equals("SHIBA INU") || name.equals("shiba inu"))
            return LtcUsdt.class;
        
        return null;
    }

}
