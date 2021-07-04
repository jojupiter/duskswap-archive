package com.dusk.duskswap.withdrawal.services;

import com.dusk.binanceExchangeRates.factories.BinanceRateFactory;
import com.dusk.binanceExchangeRates.models.BinanceRate;
import com.dusk.binanceExchangeRates.repositories.BinanceRateRepository;
import com.dusk.duskswap.account.models.AmountCurrency;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.AmountCurrencyRepository;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.Conversion;
import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.repositories.*;
import com.dusk.duskswap.withdrawal.entityDto.SellDto;
import com.dusk.duskswap.withdrawal.entityDto.SellPriceDto;
import com.dusk.duskswap.withdrawal.models.Sell;
import com.dusk.duskswap.withdrawal.repositories.SellRepository;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Status;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
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
public class SellServiceImpl implements SellService {

    @Autowired
    private SellRepository sellRepository;
    @Autowired
    private BinanceRateRepository binanceRateRepository;
    @Autowired
    private BinanceRateFactory binanceRateFactory;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ExchangeAccountRepository exchangeAccountRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private TransactionOptionRepository transactionOptionRepository;
    @Autowired
    private ConversionRepository conversionRepository;
    @Autowired
    private AmountCurrencyRepository amountCurrencyRepository;
    //@Autowired
    //private VerificationCodeRepository verificationCodeRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AccountService accountService;
    private Logger logger = LoggerFactory.getLogger(SellServiceImpl.class);

    @Override
    public ResponseEntity<List<Sell>> getAllSales(String userEmail) {
        // input checking
        if(userEmail == null || (userEmail != null && userEmail.isEmpty()))
            return ResponseEntity.badRequest().body(null);

        Optional<User> user = userRepository.findByEmail(userEmail);
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllSales :: SellServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user.get());
        if(!exchangeAccount.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getAllSales :: SellServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(sellRepository.findByExchangeAccount(exchangeAccount.get()));
    }

    @Override
    public ResponseEntity<List<Sell>> getAllSales() {
        return ResponseEntity.ok(sellRepository.findAll());
    }

    @Override
    public ResponseEntity<SellPriceDto> calculateSale(SellDto sellDto) {
        // input checking
        if(sellDto == null)
            return ResponseEntity.badRequest().body(null);

        // After checking the input, we then verify if info provided are correct
        Optional<User> user = userRepository.findByEmail(jwtUtils.getEmailFromJwtToken(sellDto.getJwtToken()));
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> calculateSale :: SellServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user.get());
        if(!exchangeAccount.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> calculateSale :: SellServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Optional<Currency> currency = currencyRepository.findById(sellDto.getFromCurrencyId());
        if(!currency.isPresent()) {
            logger.error("[" + new Date() + "] => CURRENCY ACCOUNT NOT PRESENT >>>>>>>> calculateSale :: SellServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // Here we check if the amount to be sold is less than the current balance
        Optional<AmountCurrency> amountCurrency = amountCurrencyRepository.findByAccountAndCurrencyId(exchangeAccount.get().getId(), currency.get().getId());
        if(!amountCurrency.isPresent()) {
            logger.error("[" + new Date() + "] => AMOUNT CURRENCY NOT PRESENT FOR THE ACCOUNT > "+exchangeAccount.get().getId()+
                    "< FOR CURRENCY >"+ currency.get().getIso() +"< >>>>>>>> calculateSale :: SellServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(Double.parseDouble(sellDto.getAmount()) > Double.parseDouble(amountCurrency.get().getAmount())) {
            logger.error("[" + new Date() + "] => SUPPLIED AMOUNT OF CURRENCY HIGHER THAN AVAILABLE AMOUNT >>>>>>>> calculateSale :: SellServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // then, we get the session close price of the crypto
        Class<?> currencyBinanceClassName = binanceRateFactory.getBinanceClassFromName(currency.get().getIso()); // here, we ask the class name of the currency because we want to assign it to the corresponding binanceRate class
        if(currencyBinanceClassName == null)
        {
            logger.error("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL >>>>>>>> calculateSale :: SellServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        BinanceRate rate = binanceRateRepository.findLastCryptoUsdRecord(currencyBinanceClassName);

        // now, we proceed to price calculations (when selling, the price is calculated in USDT. USDT is the default currency in our system (stablecoin))
        double cryptoAmount = Double.parseDouble(sellDto.getAmount()); // conversion of crypto currency amount in double
        double cryptoClosePrice = Double.parseDouble(rate.getTicks().getClose()); // crypto close price
        // TODO: Add duskswap fees
        String sellPrice = calculatePrice(cryptoAmount, cryptoClosePrice);

        SellPriceDto sellPriceDto = new SellPriceDto();
        sellPriceDto.setAmount(sellDto.getAmount());
        sellPriceDto.setFromCurrency(currency.get().getIso());
        sellPriceDto.setUsdPrice(sellPrice);

        return ResponseEntity.ok(sellPriceDto);
    }


    @Override
    @Transactional
    public Sell createSale(SellDto sellDto) {
        // =========> input checking
        if(sellDto == null)
            return null;

        // =========> we get necessary elements from sellDto to create a sale
        Optional<User> user = userRepository.findByEmail(jwtUtils.getEmailFromJwtToken(sellDto.getJwtToken()));
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> createSale :: SellServiceImpl.java");
            return null;
        }

        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user.get());
        if(!exchangeAccount.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> createSale :: SellServiceImpl.java");
            return null;
        }
        Optional<Currency> fromCurrency = currencyRepository.findById(sellDto.getFromCurrencyId());
        if(!fromCurrency.isPresent()) {
            logger.error("[" + new Date() + "] => CURRENCY NOT PRESENT >>>>>>>> createSale :: SellServiceImpl.java");
            return null;
        }

        Optional<Currency> toCurrency = sellDto.getToCurrencyId() == null ? currencyRepository.findByIso("USD") :
                                                                            currencyRepository.findById(sellDto.getToCurrencyId());

        Optional<TransactionOption> transactionOption = transactionOptionRepository.findById(sellDto.getTransactionOptId());
        if(!transactionOption.isPresent()) {
            logger.error("[" + new Date() + "] => TRANSACTION OPTION NOT PRESENT >>>>>>>> createSale :: SellServiceImpl.java");
        }
        Class<?> currencyBinanceClassName = binanceRateFactory.getBinanceClassFromName(fromCurrency.get().getIso()); // here, we ask the class name of the currency because we want to assign it to the corresponding binanceRate class

        if(currencyBinanceClassName == null)
        {
            logger.error("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL >>>>>>>> createSale :: SellServiceImpl.java");
            return null;
        }

        // =========> Then, we create a sale and persist it to database
        BinanceRate rate = binanceRateRepository.findLastCryptoUsdRecord(currencyBinanceClassName);

        Sell sell = new Sell();
        sell.setAmount(sellDto.getAmount());
        sell.setTel(sellDto.getTel());
        sell.setCurrency(fromCurrency.get());
        sell.setTransactionOption(transactionOption.get());
        sell.setExchangeAccount(exchangeAccount.get());

        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_TRANSACTION_CONFIRMED);
        if(!status.isPresent()) {
            logger.error("[" + new Date() + "] => STATUS NOT FOUND >>>>>>>> createSale :: SellServiceImpl.java");
            return null;
        }
        sell.setStatus(status.get());

        Conversion conversion = new Conversion();
        double cryptoAmount = Double.parseDouble(sellDto.getAmount()); // conversion of crypto currency amount in double
        double cryptoClosePrice = Double.parseDouble(rate.getTicks().getClose()); // crypto close price
        // TODO: Add duskswap fees
        String sellPrice = calculatePrice(cryptoAmount, cryptoClosePrice);
        conversion.setDuskPrice(sellPrice); // TODO: Replace sellPrice by the price of crypto currency that we had fixed in duskswap
        conversion.setFromCurrency(fromCurrency.get().getIso());
        conversion.setMarketPrice(rate.getTicks().getClose());
        conversion.setToCurrency(toCurrency.get().getIso());
        conversionRepository.save(conversion);
        sell.setConversion(conversion);

        Sell savedSell = sellRepository.save(sell);

        // ==========> finally, we debit the account
        accountService.debitAccount(exchangeAccount.get(),
                fromCurrency.get(),
                sellDto.getAmount());

        return savedSell;
    }

    private String calculatePrice(double cryptoAmount, double cryptoCloseAmount) {
        return Double.toString(cryptoAmount * cryptoCloseAmount);
    }
}
