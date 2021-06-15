package com.dusk.duskswap.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.dusk.duskswap.*", "com.dusk.shared.*"})
@EnableJpaRepositories(basePackages = {"com.dusk.duskswap.*", "com.dusk.shared.*"})
@EntityScan(basePackages = {"com.dusk.duskswap.*", "com.dusk.shared.*"})
@EnableScheduling
@PropertySource("classpath:application.properties")
public class DuskswapApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(DuskswapApplication.class, args);
	}

	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(DuskswapApplication.class);
	}

}
