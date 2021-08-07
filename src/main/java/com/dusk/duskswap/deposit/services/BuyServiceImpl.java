package com.dusk.duskswap.deposit.services;

import com.dusk.binanceExchangeRates.factories.BinanceRateFactory;
import com.dusk.binanceExchangeRates.models.BinanceRate;
import com.dusk.binanceExchangeRates.repositories.BinanceRateRepository;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.miscellaneous.Utilities;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Pricing;
import com.dusk.duskswap.commons.models.Status;
import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.dusk.duskswap.commons.repositories.PricingRepository;
import com.dusk.duskswap.commons.repositories.StatusRepository;
import com.dusk.duskswap.commons.repositories.TransactionOptionRepository;
import com.dusk.duskswap.deposit.entityDto.BuyDto;
import com.dusk.duskswap.deposit.entityDto.BuyPage;
import com.dusk.duskswap.deposit.models.Buy;
import com.dusk.duskswap.deposit.repositories.BuyRepository;
import com.dusk.duskswap.usersManagement.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

@Service
public class BuyServiceImpl implements BuyService {

    @Autowired
    private BuyRepository buyRepository;
    @Autowired
    private ExchangeAccountRepository exchangeAccountRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private PricingRepository pricingRepository;
    @Autowired
    private TransactionOptionRepository transactionOptionRepository;
    @Autowired
    private BinanceRateFactory binanceRateFactory;
    @Autowired
    private BinanceRateRepository binanceRateRepository;
    private Logger logger = LoggerFactory.getLogger(BuyServiceImpl.class);

    @Transactional
    @Override
    public Buy createBuy(User user, BuyDto dto, String payToken, String notifToken, String apiFees) throws Exception{
        // input checking
        if(dto == null) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 1. here we get the corresponding exchange account and verify if exists
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user);
        if(!exchangeAccount.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 2. then, we get the currency
        Optional<Currency> currency = currencyRepository.findById(dto.getToCurrencyId());
        if(!currency.isPresent()) {
            logger.error("[" + new Date() + "] => CURRENCY NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => CURRENCY NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 3. next, we get the transaction option
        Optional<TransactionOption> transactionOption = transactionOptionRepository.findById(dto.getTransactionOptId());
        if(!transactionOption.isPresent()) {
            logger.error("[" + new Date() + "] => TRANSACTION OPT NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => TRANSACTION OPT NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 4. Here we get the "transaction processing/in_confirmation" status to assign it to the new buy command
        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_TRANSACTION_IN_CONFIRMATION);
        if(!status.isPresent()) {
            logger.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 5. Then we check if it's possible for the user to make a deposit by looking at the min and max authorized pricing value
        Optional<Pricing> pricing = pricingRepository.findByLevelAndCurrency(user.getLevel(), currency.get());
        if(!pricing.isPresent()) {
            logger.error("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        if(
                Double.parseDouble(dto.getAmount()) > Double.parseDouble(pricing.get().getBuyMax()) ||
                Double.parseDouble(dto.getAmount()) < Double.parseDouble(pricing.get().getBuyMin())
        ) {
            logger.error("[" + new Date() + "] => CAN'T MAKE DEPOSIT (The amount is too high/low for the authorized amount) >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => CAN'T MAKE DEPOSIT (The amount is too high/low for the authorized amount) >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        // After getting the necessary elements, we create the buy command
        Buy buy = new Buy();
        buy.setNotifToken(notifToken);
        buy.setPayToken(payToken);
        buy.setExchangeAccount(exchangeAccount.get());
        buy.setTransactionOption(transactionOption.get());
        buy.setToCurrency(currency.get());
        buy.setStatus(status.get());
        buy.setApiFees(apiFees);
        buy.setTotalAmount(dto.getAmount());

        return buyRepository.save(buy);
    }

    @Transactional
    @Override
    public Buy updateBuy(String notifToken, String statusString) throws Exception {
        // input checking
        if(
                notifToken == null || (notifToken != null && notifToken.isEmpty()) ||
                statusString == null || (statusString != null && notifToken.isEmpty())
        ) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 1. we get the saved buy according to its notif token
        Optional<Buy> buy = buyRepository.findByNotifToken(notifToken);
        if(!buy.isPresent()) {
            logger.error("[" + new Date() + "] => BUY NOT PRESENT >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => BUY NOT PRESENT >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            //return null;
        }
        // >>>>> 2. and we get the corresponding status too
        Optional<Status> status = statusRepository.findByName(statusString);
        if(!status.isPresent()) {
            logger.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            //return null;
        }

        // =============================== price calculation ==================================
        // >>>>> 3. Get the pricing
        Optional<Pricing> pricing = pricingRepository.findByLevelAndCurrency(buy.get().getExchangeAccount().getUser().getLevel(),
                                                                             buy.get().getToCurrency());
        if(!pricing.isPresent()) {
            logger.error("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 4. get the conversion of the destination currency in USDT (example: btc -> usdt)
        Class<?> currencyBinanceClassName = binanceRateFactory.getBinanceClassFromName(buy.get().getToCurrency().getIso()); // here, we ask the class name of the currency because we want to assign it to the corresponding binanceRate class
        if(currencyBinanceClassName == null)
        {
            logger.error("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL ("+ buy.get().getToCurrency().getIso() + " - USDT) >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL ("+ buy.get().getToCurrency().getIso() + " - USDT) >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            //return null;
        }
        BinanceRate usdtRate = binanceRateRepository.findLastCryptoUsdRecord(currencyBinanceClassName);
        if(usdtRate == null) {
            logger.error("[" + new Date() + "] => BINANCE RATE NULL ("+ buy.get().getToCurrency().getIso()+ " - USDT) >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => BINANCE RATE NULL ("+ buy.get().getToCurrency().getIso()+ " - USDT) >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 5. we look at the pair EUR/USDT to have the value in euros
        Class<?> eurUsdtBinanceClassName = binanceRateFactory.getBinanceClassFromName(DefaultProperties.CURRENCY_EUR_ISO);
        if(currencyBinanceClassName == null)
        {
            logger.error("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL (USDT-EUR) >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL (USDT-EUR) >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            //return null;
        }
        BinanceRate eurUsdtRate = binanceRateRepository.findLastCryptoUsdRecord(eurUsdtBinanceClassName);
        if(usdtRate == null) {
            logger.error("[" + new Date() + "] => BINANCE RATE NULL (USDT-EUR) >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => BINANCE RATE NULL (USDT-EUR) >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 6. we get these conversions in variables
        Double eurToUsdt = Double.parseDouble(eurUsdtRate.getTicks().getClose());
        Double cryptoToUsdt = Double.parseDouble(usdtRate.getTicks().getClose());

        // >>>>> 7. we then calculate the fees in xaf
        if(buy.get().getApiFees() == null) {
            buy.get().setApiFees("0.0");
        }
        Double duskFeesInXaf = 0.0;
        Double duskFeesInCrypto = 0.0;
        if(pricing.get().getType().equals(DefaultProperties.PRICING_TYPE_PERCENTAGE)) {
            // here we take percentage of the initial amount
            duskFeesInCrypto = Double.parseDouble(buy.get().getTotalAmount()) * Double.parseDouble(pricing.get().getBuyFees());
            duskFeesInXaf = Utilities.convertUSdtToXaf( duskFeesInCrypto,
                    cryptoToUsdt,
                    (1.0 / eurToUsdt)
            );

        }
        if(pricing.get().getType().equals(DefaultProperties.PRICING_TYPE_FIX)) {
            // here we just convert the buy fees of pricing in xaf
            duskFeesInCrypto = Double.parseDouble(pricing.get().getBuyFees());
            duskFeesInXaf = Utilities.convertUSdtToXaf( duskFeesInCrypto,
                    cryptoToUsdt,
                    (1.0 / eurToUsdt)
            );
        }

        // >>>>> 8. we calculate the amount the user will be allocated
        //  basically, allocated amount = conversion to Crypto (initial amount (supplied in parameter) in xaf - dusk fees in xaf - api fees in xaf)
        Double amountCryptoToBeAllocatedInXaf = Utilities.convertXafToCrypto(
                Double.parseDouble(buy.get().getTotalAmount()) -
                        duskFeesInXaf -
                        Double.parseDouble(buy.get().getApiFees()),
                cryptoToUsdt,
                eurToUsdt
        );
        // =====================================================================================

        // >>>>> 9. finally we proceed to the update
        buy.get().setBuyDate(new Date());
        buy.get().setStatus(status.get());
        buy.get().setAmountCrypto(Double.toString(amountCryptoToBeAllocatedInXaf));
        buy.get().setDuskFeesCrypto(Double.toString(duskFeesInCrypto));
        buy.get().setDuskFees(Double.toString(duskFeesInXaf));

        return buyRepository.save(buy.get());
    }

    @Override
    public ResponseEntity<BuyPage> getAllBuy(Integer currentPage, Integer pageSize) {

        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Buy> buys = buyRepository.findAll(pageable);
        if(buys.hasContent()) {
            BuyPage buyPage = new BuyPage();
            buyPage.setCurrentPage(buys.getNumber());
            buyPage.setTotalItems(buys.getTotalElements());
            buyPage.setTotalNumberPages(buys.getTotalPages());
            buyPage.setBuyList(buys.getContent());

            return ResponseEntity.ok(buyPage);
        }

        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<BuyPage> getAllBuyByUser(User user, Integer currentPage, Integer pageSize) {

        // input checking
        if(user == null) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> getAllBuyByUser :: BuyServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        // after checking input, we get the user's account
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user);
        if(!exchangeAccount.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getAllBuyByUser :: BuyServiceImpl.java");
            return null;
        }

        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Buy> buys = buyRepository.findByExchangeAccount(exchangeAccount.get(), pageable);
        if(buys.hasContent()) {
            BuyPage buyPage = new BuyPage();
            buyPage.setCurrentPage(buys.getNumber());
            buyPage.setTotalItems(buys.getTotalElements());
            buyPage.setTotalNumberPages(buys.getTotalPages());
            buyPage.setBuyList(buys.getContent());

            return ResponseEntity.ok(buyPage);
        }

        return ResponseEntity.ok(null);

    }

}
