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

    @Override
    public Buy createBuy(User user, BuyDto dto, String payToken, String notifToken, String apiFees) {
        // input checking
        if(dto == null) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        // here we get the corresponding exchange account and verify if exists
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user);
        if(!exchangeAccount.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        //then, we get the currency
        Optional<Currency> currency = currencyRepository.findById(dto.getToCurrencyId());
        if(!currency.isPresent()) {
            logger.error("[" + new Date() + "] => CURRENCY NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        // next, we get the transaction option
        Optional<TransactionOption> transactionOption = transactionOptionRepository.findById(dto.getTransactionOptId());
        if(!transactionOption.isPresent()) {
            logger.error("[" + new Date() + "] => TRANSACTION OPT NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        // Here we get the "transaction processing/in_confirmation" status to assign it to the new buy command
        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_TRANSACTION_IN_CONFIRMATION);
        if(!status.isPresent()) {
            logger.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        // Then we check if it's possible for the user to make a deposit by looking at the min and max authorized pricing value
        Optional<Pricing> pricing = pricingRepository.findByLevelAndCurrency(user.getLevel(), currency.get());
        if(!pricing.isPresent()) {
            logger.error("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        if(
                Double.parseDouble(dto.getAmount()) > Double.parseDouble(pricing.get().getBuyMax()) ||
                Double.parseDouble(dto.getAmount()) < Double.parseDouble(pricing.get().getBuyMin())
        ) {
            logger.error("[" + new Date() + "] => CAN'T MAKE DEPOSIT (The amount is too high/low for the authorized amount) >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        // if the amount is in the authorized bounds, then we create the buy
   // ============================ fees calculation =========================
        // first we get the conversion fromCurrency to USDT
        Class<?> currencyBinanceClassName = binanceRateFactory.getBinanceClassFromName(currency.get().getIso()); // here, we ask the class name of the currency because we want to assign it to the corresponding binanceRate class
        if(currencyBinanceClassName == null)
        {
            logger.error("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL ("+ currency.get().getIso() + " - USDT) >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }
        BinanceRate usdtRate = binanceRateRepository.findLastCryptoUsdRecord(currencyBinanceClassName);
        if(usdtRate == null) {
            logger.error("[" + new Date() + "] => BINANCE RATE NULL ("+ currency.get().getIso()+ " - USDT) >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        // After that, we look at the pair EUR/USDT to have the value in euros
        Class<?> eurUsdtBinanceClassName = binanceRateFactory.getBinanceClassFromName(DefaultProperties.CURRENCY_EUR_ISO);
        if(currencyBinanceClassName == null)
        {
            logger.error("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL (USDT-EUR) >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }
        BinanceRate eurUsdtRate = binanceRateRepository.findLastCryptoUsdRecord(currencyBinanceClassName);
        if(usdtRate == null) {
            logger.error("[" + new Date() + "] => BINANCE RATE NULL (USDT-EUR) >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        Double eurToUsdt = Double.parseDouble(eurUsdtRate.getTicks().getClose());
        Double cryptoToUsdt = Double.parseDouble(usdtRate.getTicks().getClose());

        // Then we calculate fees
        // >>>>> dusk fees in xaf
        Double duskFeesInXaf = 0.0;
        Double duskFeesInCrypto = 0.0;
        if(pricing.get().getType().equals(DefaultProperties.PRICING_TYPE_PERCENTAGE)) {
            // here we take percentage of the initial amount
            duskFeesInCrypto = Double.parseDouble(dto.getAmount()) * Double.parseDouble(pricing.get().getBuyFees());
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

        // then we calculate the amount the user will be allocated
        //>>>>>> basically allocated amount = conversion to Crypto (initial amount (supplied in parameter) in xaf - dusk fees in xaf - api fees in xaf)
        Double amountCryptoToBeAllocatedInXaf = Utilities.convertXafToCrypto(
                Double.parseDouble(dto.getAmount()) -
                                duskFeesInXaf -
                                Double.parseDouble(apiFees),
                        cryptoToUsdt,
                        eurToUsdt
        );

        // After getting the necessary elements, we create the buy command
        Buy buy = new Buy();
        buy.setNotifToken(notifToken);
        buy.setPayToken(payToken);
        buy.setExchangeAccount(exchangeAccount.get());
        buy.setTransactionOption(transactionOption.get());
        buy.setToCurrency(currency.get());
        buy.setStatus(status.get());

        buy.setTotalAmount(dto.getAmount());
        buy.setAmountCrypto(Double.toString(amountCryptoToBeAllocatedInXaf));
        buy.setDuskFeesCrypto(Double.toString(duskFeesInCrypto));
        buy.setDuskFees(Double.toString(duskFeesInXaf));
        buy.setApiFees(apiFees);

        return buyRepository.save(buy);
    }

    @Override
    public Buy updateBuyStatus(String payToken, String statusString) {
        // input checking
        if(
                payToken == null || (payToken != null && payToken.isEmpty()) ||
                statusString == null || (statusString != null && payToken.isEmpty())
        ) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            return null;
        }

        // Now, we get the saved buy according to its pay token
        Optional<Buy> buy = buyRepository.findByPayToken(payToken);
        if(!buy.isPresent()) {
            logger.error("[" + new Date() + "] => BUY NOT PRESENT >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            return null;
        }
        // and we get the corresponding status too
        Optional<Status> status = statusRepository.findByName(statusString);
        if(!status.isPresent()) {
            logger.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            return null;
        }

        // finally we proceed to the update
        buy.get().setStatus(status.get());

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
