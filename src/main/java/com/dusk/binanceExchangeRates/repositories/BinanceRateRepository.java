package com.dusk.binanceExchangeRates.repositories;

import com.dusk.binanceExchangeRates.models.BinanceRate;

import java.util.List;

public interface BinanceRateRepository {
    List<?> findAllCryptoUsd(Class<?> binanceClass);
    BinanceRate findLastCryptoUsdRecord(Class<?> binanceClass);
}
