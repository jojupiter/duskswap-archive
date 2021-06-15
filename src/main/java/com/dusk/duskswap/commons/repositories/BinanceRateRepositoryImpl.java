package com.dusk.duskswap.commons.repositories;

import com.dusk.binanceExchangeRates.BtcUsd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BinanceRateRepositoryImpl implements BinanceRateRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<BtcUsd> findAllBtcUsd() {
        Query query = new Query();
        List<BtcUsd> btcUsds = mongoTemplate.find(query, BtcUsd.class);
        return btcUsds;
    }
}
