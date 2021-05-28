package com.dusk.duskswap.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.dusk.duskswap.*"})
@EnableJpaRepositories("com.dusk.duskswap.*")
@EntityScan(basePackages = {"com.dusk.duskswap.*"})
@EnableScheduling
@PropertySource("classpath:application.properties")
public class DuskswapApplication {

	public static void main(String[] args) {
		SpringApplication.run(DuskswapApplication.class, args);
	}

}
