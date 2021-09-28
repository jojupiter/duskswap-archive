package com.dusk.duskswap.application;

import com.dusk.externalAPIs.blockstream.services.BlockStreamService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.dusk.duskswap.*", "com.dusk.shared.*", "com.dusk.binanceExchangeRates.*", "com.dusk.externalAPIs.*"})
@EnableJpaRepositories(basePackages = {"com.dusk.duskswap.*", "com.dusk.shared.*"})
@EnableMongoRepositories(basePackages = {"com.dusk.binanceExchangeRates.*"})
@EntityScan(basePackages = {"com.dusk.duskswap.*", "com.dusk.shared.*", "com.dusk.binanceExchangeRates.*", "com.dusk.externalAPIs.*"})
@EnableScheduling
@PropertySource("classpath:application.properties")
public class DuskswapApplication /*extends SpringBootServletInitializer*/{

	public static void main(String[] args) {
		SpringApplication.run(DuskswapApplication.class, args);
	}

	/*protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(DuskswapApplication.class);
	}*/

}
