package com.dusk.duskswap.exchange.services;

import com.dusk.binanceExchangeRates.factories.BinanceRateFactory;
import com.dusk.binanceExchangeRates.models.BinanceRate;
import com.dusk.binanceExchangeRates.repositories.BinanceRateRepository;
import com.dusk.duskswap.account.models.AmountCurrency;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.AmountCurrencyRepository;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Pricing;
import com.dusk.duskswap.commons.models.Status;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.dusk.duskswap.commons.repositories.PricingRepository;
import com.dusk.duskswap.commons.repositories.StatusRepository;
import com.dusk.duskswap.exchange.entityDto.ExchangeDto;
import com.dusk.duskswap.exchange.models.Exchange;
import com.dusk.duskswap.exchange.repositories.ExchangeRepository;
import com.dusk.duskswap.usersManagement.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ExchangeServiceImpl implements ExchangeService {

    @Autowired
    private ExchangeRepository exchangeRepository;
    @Autowired
    private ExchangeAccountRepository exchangeAccountRepository;
    @Autowired
    private AmountCurrencyRepository amountCurrencyRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private PricingRepository pricingRepository;
    @Autowired
    private BinanceRateFactory binanceRateFactory;
    @Autowired
    private BinanceRateRepository binanceRateRepository;
    private Logger logger = LoggerFactory.getLogger(ExchangeServiceImpl.class);

    @Override
    public ResponseEntity<List<Exchange>> getAllExchanges() {
        return ResponseEntity.ok(exchangeRepository.findAll());
    }

    @Override
    public ResponseEntity<List<Exchange>> getAllUserExchanges(User user) {
        if(user == null) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> getAllUserExchanges :: ExchangeServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user);
        if(!exchangeAccount.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getAllUserExchanges :: ExchangeServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(exchangeRepository.findByExchangeAccount(exchangeAccount.get()));
    }

    @Transactional
    @Override
    public Exchange makeExchange(ExchangeDto dto, User user, ExchangeAccount exchangeAccount) {
        // input checking
        if(
                dto == null ||
                (dto != null &&
                        (
                                dto.getFromAmount() == null || (dto.getFromAmount() != null && dto.getFromAmount().isEmpty()) ||
                                dto.getToAmount() == null || (dto.getToAmount() != null && dto.getToAmount().isEmpty()) ||
                                dto.getFromCurrencyId() == null || dto.getToCurrencyId() == null
                        )
                ) ||
                user == null ||
                exchangeAccount == null
        ) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }
        if(testExchangePositivity(dto)) {
            logger.error("[" + new Date() + "] => INPUT VALUE NEGATIVE OR ZERO (" + dto + ") >>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }
        if(user.getLevel() == null) {
            logger.error("[" + new Date() + "] => USER'S LEVEL NOT PRESENT >>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }

        // ==================== now we get the necessary elements ============

        // >>>>> 1. the initial currency
        Optional<Currency> fromCurrency = currencyRepository.findById(dto.getFromCurrencyId());
        if(!fromCurrency.isPresent()) {
            logger.error("[" + new Date() + "] => INITIAL CURRENCY NOT PRESENT >>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }
        // >>>>> 2. the destination currency
        Optional<Currency> toCurrency = currencyRepository.findById(dto.getToCurrencyId());
        if(!toCurrency.isPresent()) {
            logger.error("[" + new Date() + "] => DESTINATION CURRENCY NOT PRESENT >>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }
        // >>>> 3. status confirmed
        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_TRANSACTION_CONFIRMED);
        if(!status.isPresent()) {
            logger.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }

        // ========================== next, we check account balance ==========
        Optional<AmountCurrency> amountCurrency = amountCurrencyRepository.findByAccountAndCurrencyId(exchangeAccount.getId(), fromCurrency.get().getId());
        if(!amountCurrency.isPresent()) {
            logger.error("[" + new Date() + "] => AMOUNT CURRENCY NOT PRESENT >>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }
        if(Double.parseDouble(amountCurrency.get().getAmount()) < Double.parseDouble(dto.getFromAmount())) {
            logger.error("[" + new Date() + "] => INSUFFICIENT BALANCE >>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }

        // ========================= check the user's pricing for exchange =========
        Optional<Pricing> pricing = pricingRepository.findByLevelAndCurrency(user.getLevel(), fromCurrency.get());
        if(!pricing.isPresent()) {
            logger.error("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }

        if(
                Double.parseDouble(dto.getFromAmount()) > Double.parseDouble(pricing.get().getExchangeMax()) ||
                Double.parseDouble(dto.getFromAmount()) < Double.parseDouble(pricing.get().getExchangeMin())
        ) {
            logger.error("[" + new Date() + "] => INITIAL AMOUNT OUT OF BOUNDS [" + pricing.get().getExchangeMin()+ "," + pricing.get().getExchangeMax() +" ]" +
                  " WITH fromAmount = " + dto.getFromAmount() + ">>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }

        // ========================= calculating the fees and the exchanged amount =====

        // >>>>>>> 1. we get the conversion fromCurrency - udst
        Class<?> fromCurrencyBinanceClassName = binanceRateFactory.getBinanceClassFromName(fromCurrency.get().getIso()); // here, we ask the class name of the currency because we want to assign it to the corresponding binanceRate class
        if(fromCurrencyBinanceClassName == null)
        {
            logger.error("[" + new Date() + "] => FROM CURRENCY BINANCE CLASS NAME NULL ("+ fromCurrency.get().getIso() + " - USDT) >>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }
        BinanceRate fromCurrencyUsdtRate = binanceRateRepository.findLastCryptoUsdRecord(fromCurrencyBinanceClassName);
        if(fromCurrencyUsdtRate == null) {
            logger.error("[" + new Date() + "] => FROM BINANCE RATE NULL ("+ fromCurrency.get().getIso()+ " - USDT) >>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }

        // >>>>>> 2. we get the conversion fromCurrency - usdt
        Class<?> toCurrencyBinanceClassName = binanceRateFactory.getBinanceClassFromName(toCurrency.get().getIso()); // here, we ask the class name of the currency because we want to assign it to the corresponding binanceRate class
        if(toCurrencyBinanceClassName == null)
        {
            logger.error("[" + new Date() + "] => TO CURRENCY BINANCE CLASS NAME NULL ("+ toCurrency.get().getIso() + " - USDT) >>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }
        BinanceRate toCurrencyUsdtRate = binanceRateRepository.findLastCryptoUsdRecord(toCurrencyBinanceClassName);
        if(toCurrencyUsdtRate == null) {
            logger.error("[" + new Date() + "] => TO BINANCE RATE NULL ("+ toCurrency.get().getIso()+ " - USDT) >>>>>>>> makeExchange :: ExchangeServiceImpl.java");
            return null;
        }

        // >>>>> 3. we calculate ratio from - to crypto
        Double fromToConversionRatio = Double.parseDouble(fromCurrencyUsdtRate.getTicks().getClose()) /
                                        Double.parseDouble(toCurrencyUsdtRate.getTicks().getClose());

        // >>>>> 4. we calculate dusk fees
        Double duskFees = 0.0;
        if(pricing.get().getType().equals(DefaultProperties.PRICING_TYPE_PERCENTAGE)) {
            duskFees = Double.parseDouble(pricing.get().getExchangeFees()) *
                       Double.parseDouble(dto.getFromAmount());
        }
        else if(pricing.get().getType().equals(DefaultProperties.PRICING_TYPE_FIX))
            duskFees = Double.parseDouble(pricing.get().getExchangeFees());

        // >>>>> 5. we then calculate the exchanged amount (formula : (from_amount - fees) * fromToConversionRate )
        Double exchangedAmount = (Double.parseDouble(dto.getFromAmount()) - duskFees) * fromToConversionRatio;

        // =========================== creating the exchange data in database ========
        Exchange exchange = new Exchange();
        exchange.setFromAmount(dto.getFromAmount());
        exchange.setToAmount(Double.toString(exchangedAmount));
        exchange.setDuskFees(Double.toString(duskFees));
        exchange.setFromCurrency(fromCurrency.get());
        exchange.setToCurrency(toCurrency.get());
        exchange.setStatus(status.get());
        exchange.setExchangeAccount(exchangeAccount);
        exchange.setFromCurrencyPriceInUsdt(fromCurrencyUsdtRate.getTicks().getClose());
        exchange.setToCurrencyPriceInUsdt(toCurrencyUsdtRate.getTicks().getClose());

        return exchangeRepository.save(exchange);

    }


    private Boolean testExchangePositivity(ExchangeDto dto) {
        return Double.parseDouble(dto.getToAmount()) > 0 && Double.parseDouble(dto.getFromAmount()) > 0;
    }

}
