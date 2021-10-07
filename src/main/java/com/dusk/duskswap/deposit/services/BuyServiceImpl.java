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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    @Override
    public Boolean existsByTxId(String txId) {
        if(txId == null)
            return null;

        return buyRepository.existsByTransactionId(txId);
    }

    @Transactional
    @Override
    public Buy createBuy(User user, BuyDto dto, String payToken, String apiFees, String txId) throws Exception{
        // input checking
        if(
                dto == null ||
                (
                    dto != null &&
                            (
                                dto.getAmount() == null || (dto.getAmount() != null && dto.getAmount().isEmpty()) || (dto.getAmount() != null && Double.parseDouble(dto.getAmount()) <= 0) ||
                                dto.getToCurrencyId() == null ||
                                dto.getTransactionOptId() == null
                            )
                )
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 1. here we get the corresponding exchange account and verify if exists
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user);
        if(!exchangeAccount.isPresent()) {
            log.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 2. then, we get the currency
        Optional<Currency> currency = currencyRepository.findById(dto.getToCurrencyId());
        if(!currency.isPresent()) {
            log.error("[" + new Date() + "] => CURRENCY NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => CURRENCY NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 3. next, we get the transaction option
        Optional<TransactionOption> transactionOption = transactionOptionRepository.findById(dto.getTransactionOptId());
        if(!transactionOption.isPresent()) {
            log.error("[" + new Date() + "] => TRANSACTION OPT NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => TRANSACTION OPT NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 4. Here we get the "transaction processing/in_confirmation" status to assign it to the new buy command
        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_TRANSACTION_INITIATED);
        if(!status.isPresent()) {
            log.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 5. Then we check if it's possible for the user to make a deposit by looking at the min and max authorized pricing value
        Optional<Pricing> pricing = pricingRepository.findByLevelAndCurrency(user.getLevel(), currency.get());
        if(!pricing.isPresent()) {
            log.error("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        if(
                Double.parseDouble(dto.getAmount()) > Double.parseDouble(pricing.get().getBuyMax()) ||
                Double.parseDouble(dto.getAmount()) < Double.parseDouble(pricing.get().getBuyMin())
        ) {
            log.error("[" + new Date() + "] => CAN'T MAKE DEPOSIT (The amount is too high/low for the authorized amount) >>>>>>>> createBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => CAN'T MAKE DEPOSIT (The amount is too high/low for the authorized amount) >>>>>>>> createBuy :: BuyServiceImpl.java");
            //return null;
        }

        // After getting the necessary elements, we create the buy command
        Buy buy = new Buy();
        buy.setPayToken(payToken);
        buy.setExchangeAccount(exchangeAccount.get());
        buy.setTransactionOption(transactionOption.get());
        buy.setToCurrency(currency.get());
        buy.setStatus(status.get());
        Double apiFeesInFiat = Double.parseDouble(apiFees) * Double.parseDouble(dto.getAmount());
        buy.setApiFees(Double.toString(apiFeesInFiat));
        buy.setTotalAmount(dto.getAmount());
        buy.setTransactionId(txId);

        return buyRepository.save(buy);
    }

    @Transactional
    @Override
    public Buy confirmBuy(Buy buy) throws Exception {
        // input checking
        if(buy == null) {
            log.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 1. we check if the current status is confirmed or invalid before continuing
        if(
                buy.getStatus().getName().equals(DefaultProperties.STATUS_TRANSACTION_CONFIRMED) ||
                buy.getStatus().getName().equals(DefaultProperties.STATUS_TRANSACTION_INVALID)
        ) {
            log.error("[" + new Date() + "] => STATUS ALREADY CONFIRMED OR INVALID >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => STATUS ALREADY CONFIRMED OR INVALID >>>>>>>> confirmBuy :: BuyServiceImpl.java");
        }

        // >>>>> 2. and we get the corresponding status too
        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_TRANSACTION_CONFIRMED);
        if(!status.isPresent()) {
            log.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            //return null;
        }

        // =============================== price calculation ==================================
        // >>>>> 3. Get the pricing
        Optional<Pricing> pricing = pricingRepository.findByLevelAndCurrency(buy.getExchangeAccount().getUser().getLevel(),
                                                                             buy.getToCurrency());
        if(!pricing.isPresent()) {
            log.error("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 4. get the conversion of the destination currency in USDT (example: btc -> usdt)
        Class<?> currencyBinanceClassName = binanceRateFactory.getBinanceClassFromName(buy.getToCurrency().getIso()); // here, we ask the class name of the currency because we want to assign it to the corresponding binanceRate class
        if(currencyBinanceClassName == null)
        {
            log.error("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL ("+ buy.getToCurrency().getIso() + " - USDT) >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL ("+ buy.getToCurrency().getIso() + " - USDT) >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            //return null;
        }
        BinanceRate usdtRate = binanceRateRepository.findLastCryptoUsdRecord(currencyBinanceClassName);
        if(usdtRate == null) {
            log.error("[" + new Date() + "] => BINANCE RATE NULL ("+ buy.getToCurrency().getIso()+ " - USDT) >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => BINANCE RATE NULL ("+ buy.getToCurrency().getIso()+ " - USDT) >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 5. we look at the pair EUR/USDT to have the value in euros
        Class<?> eurUsdtBinanceClassName = binanceRateFactory.getBinanceClassFromName(DefaultProperties.CURRENCY_EUR_ISO);
        if(currencyBinanceClassName == null)
        {
            log.error("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL (USDT-EUR) >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL (USDT-EUR) >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            //return null;
        }
        BinanceRate eurUsdtRate = binanceRateRepository.findLastCryptoUsdRecord(eurUsdtBinanceClassName);
        if(usdtRate == null) {
            log.error("[" + new Date() + "] => BINANCE RATE NULL (USDT-EUR) >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            throw new Exception("[" + new Date() + "] => BINANCE RATE NULL (USDT-EUR) >>>>>>>> confirmBuy :: BuyServiceImpl.java");
            //return null;
        }

        // >>>>> 6. we get these conversions in variables
        Double eurToUsdt = Double.parseDouble(eurUsdtRate.getTicks().getClose());
        Double cryptoToUsdt = Double.parseDouble(usdtRate.getTicks().getClose());

        // >>>>> 7. we then calculate the fees in xaf
        if(buy.getApiFees() == null) {
            buy.setApiFees("0.0");
        }
        Double duskFeesInXaf = 0.0;
        Double duskFeesInCrypto = 0.0;
        Double initialAmountFiatToCrypto = 0.0;

        if(pricing.get().getType().equals(DefaultProperties.PRICING_TYPE_PERCENTAGE)) {
            // here we take percentage of the initial amount
            initialAmountFiatToCrypto = Utilities.convertXafToCrypto(Double.parseDouble(buy.getTotalAmount()),
                    cryptoToUsdt,
                    eurToUsdt
            );
            duskFeesInCrypto = initialAmountFiatToCrypto * Double.parseDouble(pricing.get().getBuyFees());
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
        Double amountCryptoToBeAllocated = Utilities.convertXafToCrypto(
                Double.parseDouble(buy.getTotalAmount()) -
                        duskFeesInXaf -
                        Double.parseDouble(buy.getApiFees()),
                cryptoToUsdt,
                eurToUsdt
        );
        // =====================================================================================

        // >>>>> 9. finally we proceed to the update
        buy.setBuyDate(new Date());
        buy.setStatus(status.get());
        buy.setAmountCrypto(Double.toString(amountCryptoToBeAllocated));
        buy.setDuskFeesCrypto(Double.toString(duskFeesInCrypto));
        buy.setDuskFees(Double.toString(duskFeesInXaf));

        return buyRepository.save(buy);
    }

    @Override
    public Buy updateBuyStatus(Buy buy, String statusString) {
        if(
                buy == null ||
                statusString == null || (statusString != null && statusString.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT >>>>>>>> updateBuyStatus :: BuyServiceImpl.java" +
                    " ========= buy = " + buy + ", statusString = " + statusString);
            return null;
        }

        Optional<Status> status = statusRepository.findByName(statusString);
        if(!status.isPresent()) {
            log.error("[" + new Date() + "] => UNABLE TO FIND STATUS >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            return null;
        }
        buy.setStatus(status.get());

        return buyRepository.save(buy);
    }

    @Override
    public Optional<Buy> getByTransactionId(String transactionId) {
        if(transactionId == null || (transactionId != null && transactionId.isEmpty()))
            return Optional.empty();

        return buyRepository.findByTransactionId(transactionId);
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
            log.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> getAllBuyByUser :: BuyServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        // after checking input, we get the user's account
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user);
        if(!exchangeAccount.isPresent()) {
            log.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getAllBuyByUser :: BuyServiceImpl.java");
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
