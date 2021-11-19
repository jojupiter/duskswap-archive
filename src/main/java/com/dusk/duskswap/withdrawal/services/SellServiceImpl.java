package com.dusk.duskswap.withdrawal.services;

import com.dusk.binanceExchangeRates.factories.BinanceRateFactory;
import com.dusk.binanceExchangeRates.models.BinanceRate;
import com.dusk.binanceExchangeRates.repositories.BinanceRateRepository;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.AmountCurrencyRepository;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.miscellaneous.Utilities;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Pricing;
import com.dusk.duskswap.commons.models.Status;
import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.repositories.*;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import com.dusk.duskswap.withdrawal.entityDto.SellDto;
import com.dusk.duskswap.withdrawal.entityDto.SellPage;
import com.dusk.duskswap.withdrawal.models.Sell;
import com.dusk.duskswap.withdrawal.repositories.SellRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
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
    @Autowired
    private PricingRepository pricingRepository;
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public ResponseEntity<SellPage> getAllSell(User user, Integer currentPage, Integer pageSize) {
        // input checking
        if(user == null) {
            log.error("[" + new Date() + "] => INPUT (user) NULL >>>>>>>> getAllSales :: SellServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }
        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user);
        if(!exchangeAccount.isPresent()) {
            log.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getAllSales :: SellServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Sell> sells = sellRepository.findByExchangeAccount(exchangeAccount.get(), pageable);
        if(sells.hasContent()) {
            SellPage sellPage = new SellPage();
            sellPage.setCurrentPage(sells.getNumber());
            sellPage.setTotalItems(sells.getTotalElements());
            sellPage.setTotalNumberPages(sells.getTotalPages());
            sellPage.setSells(sells.getContent());

            return ResponseEntity.ok(sellPage);
        }

        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<SellPage> getAllSell(Integer currentPage, Integer pageSize) {
        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Sell> sells = sellRepository.findAll(pageable);
        if(sells.hasContent()) {
            SellPage sellPage = new SellPage();
            sellPage.setCurrentPage(sells.getNumber());
            sellPage.setTotalItems(sells.getTotalElements());
            sellPage.setTotalNumberPages(sells.getTotalPages());
            sellPage.setSells(sells.getContent());

            return ResponseEntity.ok(sellPage);
        }
        return ResponseEntity.ok(null);
    }

    @Override
    @Transactional
    public Sell createSell(SellDto dto, User user, ExchangeAccount account, Currency fromCurrency,
                           TransactionOption transactionOption, String usdXaf, String apiFees) throws Exception {
        // input checking
        if(dto == null || user == null || account == null || fromCurrency == null || transactionOption == null) {
            log.error("[" + new Date() + "] => INPUT NULL >>>>>>>> createSale :: SellServiceImpl.java" +
                    " ======= sellDto = " + dto + ", user = " + user + ", account = " + account + ", currency = " + fromCurrency + ", tOpt = " + transactionOption);
            throw new Exception("[" + new Date() + "] => INPUT NULL >>>>>>>> createSale :: SellServiceImpl.java" +
                    " ======= sellDto = " + dto + ", user = " + user + ", account = " + account + ", currency = " + fromCurrency + ", tOpt = " + transactionOption);
        }
        // ===================== Getting necessary elements from sellDto to create a sale =================
        // >>>>> 1. we check according to the pricing, if the user is able to make
        Optional<Pricing> pricing = pricingRepository.findByLevelAndCurrency(user.getLevel(), fromCurrency);
        if(!pricing.isPresent()) {
            log.error("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> createSale :: SellServiceImpl.java");
            throw new Exception("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> createSale :: SellServiceImpl.java");
        }
        if(
                Double.parseDouble(dto.getAmount()) > Double.parseDouble(pricing.get().getSellMax()) ||
                Double.parseDouble(dto.getAmount()) < Double.parseDouble(pricing.get().getSellMin())
        ) {
            log.error("[" + new Date() + "] => INSERTED AMOUNT OUT OF BOUND (The amount is too high/low for the authorized amount) >>>>>>>> createSale :: SellServiceImpl.java");
            throw new Exception("[" + new Date() + "] => INSERTED AMOUNT OUT OF BOUND (The amount is too high/low for the authorized amount) >>>>>>>> createSale :: SellServiceImpl.java");
        }

        // >>>>> 2. get the conversion of the initial currency in USDT (example: btc -> usdt)
        Class<?> currencyBinanceClassName = binanceRateFactory.getBinanceClassFromName(fromCurrency.getIso()); // here, we ask the class name of the currency because we want to assign it to the corresponding binanceRate class
        if(currencyBinanceClassName == null)
        {
            log.error("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL >>>>>>>> createSale :: SellServiceImpl.java");
            throw new Exception("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL >>>>>>>> createSale :: SellServiceImpl.java");
            //return null;
        }
        BinanceRate usdtRate = binanceRateRepository.findLastCryptoUsdRecord(currencyBinanceClassName);
        if(usdtRate == null) {
            log.error("[" + new Date() + "] => BINANCE RATE NULL ("+ fromCurrency.getIso() + " - USDT) >>>>>>>> createSale :: SellServiceImpl.java");
            throw new Exception("[" + new Date() + "] => BINANCE RATE NULL ("+ fromCurrency.getIso() + " - USDT) >>>>>>>> createSale :: SellServiceImpl.java");
            //return null;
        }

        // >>>>> 3. we convert usdXaf to number
        Double usdToXaf = Double.parseDouble(usdXaf);

        log.info("DOLLAR TO XAF >>>>>>>>>>>>> " + usdXaf + "              :::::: SellService");

        // >>>>> 4. we get these conversions in variables
        Double cryptoToUsdt = Double.parseDouble(usdtRate.getTicks().getClose());
        log.info("BTC TO USDT >>>>>>>>>>>>> " + cryptoToUsdt + "              :::::: SellService");

        // ================================== price calculation =========================================

        // >>>>> 5. fees calculation
        Double duskFeesInFiat = 0.0;
        Double duskFeesInCrypto = 0.0;

        if(pricing.get().getTypeSell().equals(DefaultProperties.PRICING_TYPE_PERCENTAGE)) {
            // here we take percentage of the initial amount
            duskFeesInCrypto = Double.parseDouble(dto.getAmount()) * Double.parseDouble(pricing.get().getSellFees());
            duskFeesInFiat = Utilities.convertUSdtToXaf( duskFeesInCrypto,
                    cryptoToUsdt,
                    usdToXaf
            );
        }
        if(pricing.get().getTypeSell().equals(DefaultProperties.PRICING_TYPE_FIX)) {
            // here we just convert the buy fees of pricing in xaf
            duskFeesInCrypto = Double.parseDouble(pricing.get().getSellFees());
            duskFeesInFiat = Utilities.convertUSdtToXaf( duskFeesInCrypto,
                    cryptoToUsdt,
                    usdToXaf
            );
        }

        log.info("duskFeesInCrypto =  >>>>>>>>>>>>> " + duskFeesInCrypto + "              :::::: SellService");
        log.info("duskFeesInFiat =  >>>>>>>>>>>>> " + duskFeesInFiat + "              :::::: SellService");

        // api fees
        Double apiFeesAmountInCrypto = Double.parseDouble(dto.getAmount()) * Double.parseDouble(apiFees);
        Double apiFeesAmount = apiFeesAmountInCrypto *
                cryptoToUsdt *
                usdToXaf
        ; // We do multiply it here because Utilities.convertUsdtToXaf takes the floor (Math.floor), but we don't want to take the floor here
        log.info(">>>>>>>>>>>>> apiFeesAmountInCrypto = " + apiFeesAmountInCrypto + "\n  apiFeesAmount = " + apiFeesAmount + "         :::::: SellService");


        // >>>>> 6. amount to be received by the user
        // sold amount = conversion to Fiat (initial amount (supplied in parameter) in crypto - dusk fees in crypto - api_fees_in_crypto) - apiFees in Fiat
        Double amountToBeReceivedInFiat = Utilities.convertUSdtToXaf(
            Double.parseDouble(dto.getAmount()) - duskFeesInCrypto - apiFeesAmountInCrypto,
                cryptoToUsdt,
                usdToXaf
        );

        // adding the extra decimal amount to duskFees
        duskFeesInFiat += amountToBeReceivedInFiat - Math.floor(amountToBeReceivedInFiat);
        amountToBeReceivedInFiat -= (amountToBeReceivedInFiat - Math.floor(amountToBeReceivedInFiat));

        log.info("NEW Dusk fees in XAF =  >>>>>>>>>>>>> " + duskFeesInFiat + "              :::::: SellService");
        log.info("amountToBeReceivedInFiat =  >>>>>>>>>>>>> " + amountToBeReceivedInFiat + "              :::::: SellService");

        // >>>>> 7. finally, we create the sell object and we save it in the DB
        Sell sell = new Sell();
        sell.setSellDate(new Date());
        sell.setApiFees(Double.toString(apiFeesAmount));
        sell.setApiFeesCrypto(Double.toString(apiFeesAmountInCrypto));
        sell.setTotalAmountCrypto(dto.getAmount());
        sell.setAmountReceived(Double.toString(amountToBeReceivedInFiat));
        sell.setDuskFeesCrypto(Double.toString(duskFeesInCrypto));
        sell.setDuskFees(Double.toString(duskFeesInFiat));
        sell.setTel(dto.getTel());
        sell.setCurrency(fromCurrency);
        sell.setCryptoPriceInUsdt(usdtRate.getTicks().getClose());
        sell.setUsdToFiat(usdXaf);
        sell.setTransactionOption(transactionOption);
        sell.setExchangeAccount(account);
        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_TRANSACTION_INITIATED);
        if(!status.isPresent()) {
            log.error("[" + new Date() + "] => STATUS NOT FOUND >>>>>>>> createSale :: SellServiceImpl.java");
        }
        sell.setStatus(status.get());
        return sell;
    }

    @Override
    public Sell saveSell(Sell sell, String apiTransactionId) {
        if(sell == null)
            return null;
        sell.setApiTransactionId(apiTransactionId);
        return sellRepository.save(sell);
    }

    @Override
    public Sell updateSellStatus(Sell sell, String statusString) {
        if(
                sell == null ||
                statusString == null || (statusString != null && statusString.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT NULL >>>>>>>> updateSaleStatus :: SellServiceImpl.java" +
                    " ====== sell = " + sell + ", statusString = " + statusString);
            return null;
        }

        Optional<Status> status = statusRepository.findByName(statusString);
        if(!status.isPresent()) {
            log.error("[" + new Date() + "] => STATUS NOT FOUND >>>>>>>> updateSaleStatus :: SellServiceImpl.java");
            return null;
        }

        sell.setStatus(status.get());
        return sellRepository.save(sell);
    }

    @Override
    public Boolean existsByTxId(String txId) {
        if(txId == null || (txId != null && txId.isEmpty()))
            return null;
        return sellRepository.existsByTransactionId(txId);
    }

    @Override
    public Optional<Sell> getSellByTransactionId(String transactionId) {
        if(transactionId == null || (transactionId != null && transactionId.isEmpty()))
            return Optional.empty();
        return sellRepository.findByTransactionId(transactionId);
    }

    @Override
    public ResponseEntity<String> getAllSellProfits() {
        List<Sell> sells = sellRepository.findAll();
        if(sells == null || (sells != null && sells.isEmpty()))
            return ResponseEntity.ok("0.0");
        Double profits = 0.0;
        for(Sell sell : sells) {
            profits += Double.parseDouble(sell.getDuskFees());
        }
        return ResponseEntity.ok(Double.toString(profits));
    }

    @Override
    public ResponseEntity<String> getAllSellProfitsBefore(Date date) {
        List<Sell> sells = sellRepository.findBySellDateBefore(date);
        if(sells == null || (sells != null && sells.isEmpty()))
            return ResponseEntity.ok("0.0");
        Double profits = 0.0;
        for(Sell sell : sells) {
            profits += Double.parseDouble(sell.getDuskFees());
        }
        return ResponseEntity.ok(Double.toString(profits));
    }

    @Override
    public ResponseEntity<String> getAllSellProfitsAfter(Date date) {
        List<Sell> sells = sellRepository.findBySellDateAfter(date);
        if(sells == null || (sells != null && sells.isEmpty()))
            return ResponseEntity.ok("0.0");
        Double profits = 0.0;
        for(Sell sell : sells) {
            profits += Double.parseDouble(sell.getDuskFees());
        }
        return ResponseEntity.ok(Double.toString(profits));
    }

    @Override
    public ResponseEntity<String> getAllSellProfitsBetween(Date startDate, Date endDate) {
        List<Sell> sells = sellRepository.findBySellDateBetween(startDate, endDate);
        if(sells == null || (sells != null && sells.isEmpty()))
            return ResponseEntity.ok("0.0");
        Double profits = 0.0;
        for(Sell sell : sells) {
            profits += Double.parseDouble(sell.getDuskFees());
        }
        return ResponseEntity.ok(Double.toString(profits));
    }
}
