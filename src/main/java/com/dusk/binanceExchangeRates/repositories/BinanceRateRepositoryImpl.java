package com.dusk.binanceExchangeRates.repositories;

import com.dusk.binanceExchangeRates.models.BinanceRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BinanceRateRepositoryImpl implements BinanceRateRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<?> findAllCryptoUsd(Class<?> binanceClass) {
        Query query = new Query();
        List<?> cryptoUsds = mongoTemplate.find(query, binanceClass);
        return cryptoUsds;
    }

    @Override
    public BinanceRate findLastCryptoUsdRecord(Class<?> binanceClass) {
        Query query = new Query();
        query.limit(1);
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        BinanceRate cryptoUsd = (BinanceRate) mongoTemplate.findOne(query, binanceClass);

        return cryptoUsd;
    }

}
