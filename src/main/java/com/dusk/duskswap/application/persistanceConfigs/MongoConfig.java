package com.dusk.duskswap.application.persistanceConfigs;

import com.dusk.binanceExchangeRates.factories.BinanceRateFactory;
import com.dusk.duskswap.application.AppProperties;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClient mongo() {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:"+ AppProperties.MONGO_DATABASE_PORT +"/" + AppProperties.MONGO_DATABASE_NAME);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongo(), AppProperties.MONGO_DATABASE_NAME);
    }

    @Bean
    public BinanceRateFactory binanceRateFactory() {
        return new BinanceRateFactory();
    }

}
