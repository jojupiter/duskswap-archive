package com.dusk.duskswap.application;

import com.dusk.binanceExchangeRates.factories.BinanceRateFactory;
import com.dusk.binanceExchangeRates.repositories.BinanceRateRepository;
import com.dusk.duskswap.commons.repositories.VerificationCodeRepository;
import com.dusk.duskswap.commons.services.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.dusk.duskswap.*", "com.dusk.shared.*", "com.dusk.binanceExchangeRates.*"})
@EnableJpaRepositories(basePackages = {"com.dusk.duskswap.*", "com.dusk.shared.*"})
@EnableMongoRepositories(basePackages = {"com.dusk.binanceExchangeRates.*"})
@EntityScan(basePackages = {"com.dusk.duskswap.*", "com.dusk.shared.*", "com.dusk.binanceExchangeRates.*"})
@EnableScheduling
@PropertySource("classpath:application.properties")
public class DuskswapApplication /*extends SpringBootServletInitializer*/ implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DuskswapApplication.class, args);
	}

	@Autowired
	private VerificationCodeRepository verificationCodeRepository;

	@Override
	public void run(String... args) throws Exception {
		//System.out.println(verificationCodeRepository.existsByUserEmailAndPurpose("srgseraphin14@gmail.com", /*102081, */"SIGN_UP"));
	}

	/*protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(DuskswapApplication.class);
	}*/

}
