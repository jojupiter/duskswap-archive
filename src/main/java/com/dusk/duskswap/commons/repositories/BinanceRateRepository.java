package com.dusk.duskswap.commons.repositories;

import com.dusk.binanceExchangeRates.BtcUsd;
import java.util.List;

public interface BinanceRateRepository {
    List<BtcUsd> findAllBtcUsd();
}
