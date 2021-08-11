package com.dusk.duskswap.account.services;

import com.dusk.duskswap.account.entityDto.CryptoBalance;
import com.dusk.duskswap.account.models.AmountCurrency;
import com.dusk.duskswap.account.models.AmountCurrencyKey;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.AmountCurrencyRepository;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import com.dusk.duskswap.withdrawal.services.SellServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private ExchangeAccountRepository exchangeAccountRepository;
    @Autowired
    private AmountCurrencyRepository amountCurrencyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    private Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Override
    public ResponseEntity<ExchangeAccount> createExchangeAccount(User user) {

        // first, we check the input
        if(user == null ) {
            logger.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> createExchangeAccount :: AccountServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        // here we check the status of user, if it's not activated we can't create an account
        if(!user.getStatus().getName().equals(DefaultProperties.STATUS_USER_ACTIVATED))
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);

        // then we proceed to the creation, by initializing the "based" currencies the user will use
        if(exchangeAccountRepository.existsByUser(user))
            return ResponseEntity.ok(null);

        ExchangeAccount exchangeAccount = new ExchangeAccount();
        exchangeAccount.setUser(user);

        List<String> currencyStrings = new ArrayList<>();
        /*currencyStrings.add("USD");
        currencyStrings.add("BTC");
        currencyStrings.add("ETH");
        currencyStrings.add("LTC");*/

        List<Currency> currencies = currencyRepository.findByIsSupported(true);//currencyRepository.findByIsoIn(currencyStrings);

        // we save exchange account here
        ExchangeAccount newExchangeAccount = exchangeAccountRepository.save(exchangeAccount);

        Set<AmountCurrency> amountCurrencies = new HashSet<>();

        currencies.forEach(
                currency -> {
                    AmountCurrencyKey key = new AmountCurrencyKey();
                    key.setCurrencyId(currency.getId());
                    key.setExchangeAccountId(newExchangeAccount.getId());

                    AmountCurrency amountCurrency = new AmountCurrency();
                    amountCurrency.setAmount(Double.toString(0.0));
                    amountCurrency.setCurrency(currency);
                    amountCurrency.setExchangeAccount(newExchangeAccount);
                    amountCurrency.setId(key);

                    amountCurrencies.add(amountCurrency);
                }
        );

        amountCurrencyRepository.saveAll(amountCurrencies);

        return ResponseEntity.ok(newExchangeAccount);
    }

    @Override
    public ResponseEntity<ExchangeAccount> getExchangeAccount(User user) {
        // input checking
        if(user == null) {
            logger.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> getExchangeAccount :: AccountServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

         return ResponseEntity.ok(exchangeAccountRepository.findByUser(user).get());
    }

    @Override
    public ExchangeAccount getAccountByUser(User user) {
        // input checking
        if(user == null) {
            logger.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> getAccountByUserEmail :: AccountServiceImpl.java");
            return null;
        }

        return exchangeAccountRepository.findByUser(user).get();
    }

    @Override
    public ResponseEntity<List<ExchangeAccount>> getAllExchangeAccounts() {
        return ResponseEntity.ok(exchangeAccountRepository.findAll());
    }

    @Override
    public ExchangeAccount getAccountById(Long accountId) {
        return exchangeAccountRepository.findById(accountId).get();
    }

    @Override
    public ResponseEntity<List<CryptoBalance>> getUserAccountBalance(User user) {
        // input checking
        if(user == null) {
            logger.error("[" + new Date() + "] => INPUT (USER) NULL OR EMPTY >>>>>>>> getUserAccountBalance :: AccountServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        // >>>>> 1. We get the exchange account
        Optional<ExchangeAccount> account = exchangeAccountRepository.findByUser(user);
        if(!account.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getUserAccountBalance :: AccountServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 2. we get the amount currency list
        List<AmountCurrency> amountCurrencies = amountCurrencyRepository.findByAccountId(account.get().getId());
        if(amountCurrencies == null || (amountCurrencies != null && amountCurrencies.isEmpty())) {
            logger.error("[" + new Date() + "] => NO AMOUNT CURRENCY FOUND >>>>>>>> getUserAccountBalance :: AccountServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. we create list of crypto balance objects and affect their values
        List<CryptoBalance> cryptoBalances = new ArrayList<>();
        amountCurrencies.forEach(
                amountCurrency -> {
                    if(amountCurrency.getCurrency().getType().equals(DefaultProperties.CURRENCY_TYPE_CRYPTO)) {
                        CryptoBalance cryptoBalance = new CryptoBalance();
                        cryptoBalance.setId(amountCurrency.getCurrency().getId());
                        cryptoBalance.setAmount(amountCurrency.getAmount());
                        cryptoBalance.setCrypto(amountCurrency.getCurrency().getName());
                        cryptoBalance.setIso(amountCurrency.getCurrency().getIso());
                        cryptoBalances.add(cryptoBalance);
                    }
                }
        );

        return ResponseEntity.ok(cryptoBalances);
    }

    @Override
    public ResponseEntity<CryptoBalance> getUserCryptoBalance(User user, String cryptoIso) {
        // inputs checking
        if(
                user == null ||
                cryptoIso == null || (cryptoIso != null && cryptoIso.isEmpty())
        ) {
            logger.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> getUserCryptoBalance :: AccountServiceImpl.java" +
                    " ===== user = " + user + ", cryptoIso = " + cryptoIso);
            return ResponseEntity.badRequest().body(null);
        }

        // >>>>> 1. We get the exchange account
        Optional<ExchangeAccount> account = exchangeAccountRepository.findByUser(user);
        if(!account.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getUserCryptoBalance :: AccountServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 2. We get the corresponding currency
        Optional<Currency> currency = currencyRepository.findByIso(cryptoIso);
        if(!currency.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getUserCryptoBalance :: AccountServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. we then get the amount currency according to the account and the currency
        Optional<AmountCurrency> amountCurrency = amountCurrencyRepository.findByAccountAndCurrencyId(account.get().getId(), currency.get().getId());
        if(!amountCurrency.isPresent()) {
            logger.error("[" + new Date() + "] => AMOUNT CURRENCY NOT PRESENT >>>>>>>> getUserCryptoBalance :: AccountServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 4. we create the object crypto balance and return it
        CryptoBalance cryptoBalance = new CryptoBalance();
        cryptoBalance.setId(currency.get().getId());
        cryptoBalance.setIso(cryptoIso);
        cryptoBalance.setCrypto(currency.get().getName());
        cryptoBalance.setAmount(amountCurrency.get().getAmount());

        return ResponseEntity.ok(cryptoBalance);
    }

    @Override
    @Transactional
    public AmountCurrency fundAccount(ExchangeAccount account, Currency currency, String amount) throws Exception {
        // inputs checking
        if(amount == null || currency == null || account == null) {
            logger.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> fundAccount :: AccountServiceImpl.java" +
                    " ===== amount = " + amount + ", currency = " + currency + ", account = " + account);
            throw new Exception("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> fundAccount :: AccountServiceImpl.java" +
                    " ===== amount = " + amount + ", currency = " + currency + ", account = " + account);
            //return null;
        }

        Optional<AmountCurrency> amountCurrency = amountCurrencyRepository.findByAccountAndCurrencyId(account.getId(), currency.getId());
        if(!amountCurrency.isPresent()) {
            logger.error("[" + new Date() + "] => AMOUNT CURRENCY NOT PRESENT >>>>>>>> fundAccount :: AccountServiceImpl.java");
            throw new Exception("[" + new Date() + "] => AMOUNT CURRENCY NOT PRESENT >>>>>>>> fundAccount :: AccountServiceImpl.java");
            //return null;
        }

        Double currentAmount = Double.parseDouble(amountCurrency.get().getAmount());
        Double amountToAdd = Double.parseDouble(amount);

        if(amountToAdd < 0) {// we can't add a negative amount
            logger.error("[" + new Date() + "] => WE CAN'T ADD A NEGATIVE NUMBER (amountToAdd="+amountToAdd+")>>>>>>>> fundAccount :: AccountServiceImpl.java");
            throw new Exception("[" + new Date() + "] => WE CAN'T ADD A NEGATIVE NUMBER (amountToAdd="+amountToAdd+")>>>>>>>> fundAccount :: AccountServiceImpl.java");
            //return null;
        }

        amountCurrency.get().setAmount(Double.toString(currentAmount + amountToAdd));

        return amountCurrencyRepository.save(amountCurrency.get());
    }

    @Override
    @Transactional
    public AmountCurrency debitAccount(ExchangeAccount account, Currency currency, String amount) throws Exception {
        // input checking
        if(amount == null || currency == null || account == null) {
            logger.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> debitAccount :: AccountServiceImpl.java" +
                    " ===== amount = " + amount + ", currency = " + currency + ", account = " + account);
            throw new Exception("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> fundAccount :: AccountServiceImpl.java" +
                    " ===== amount = " + amount + ", currency = " + currency + ", account = " + account);
            //return null;
        }

        Optional<AmountCurrency> amountCurrency = amountCurrencyRepository.findByAccountAndCurrencyId(account.getId(), currency.getId());
        if(!amountCurrency.isPresent()) {
            logger.error("[" + new Date() + "] => AMOUNT CURRENCY NOT PRESENT >>>>>>>> debitAccount :: AccountServiceImpl.java");
            throw new Exception("[" + new Date() + "] => AMOUNT CURRENCY NOT PRESENT >>>>>>>> debitAccount :: AccountServiceImpl.java");
            //return null;
        }

        Double currentAmount = Double.parseDouble(amountCurrency.get().getAmount());
        Double amountToRemove = Double.parseDouble(amount);

        if(amountToRemove < 0 || amountToRemove > currentAmount) {
            logger.error("[" + new Date() + "] => AMOUNT CURRENCY NOT PRESENT >>>>>>>> debitAccount :: AccountServiceImpl.java");
            throw new Exception("[" + new Date() + "] => AMOUNT CURRENCY NOT PRESENT >>>>>>>> debitAccount :: AccountServiceImpl.java");
            //return null;
        }

        amountCurrency.get().setAmount(Double.toString(currentAmount - amountToRemove));

        return amountCurrencyRepository.save(amountCurrency.get());
    }

    @Override
    public boolean isBalanceSufficient(ExchangeAccount account, Long currencyId, String amount) {
        // input checking
        if(account == null || currencyId == null || amount == null || (amount != null && amount.isEmpty()))
        {
            logger.error("WRONG INPUT >>>>>>>> isBalanceSufficient :: AccountServiceImpl.java  ========== Account = " + account +
                    ", currencyId = " + currencyId + ", amount = " + amount);
            return false;
        }

        Optional<AmountCurrency> amountCurrency = amountCurrencyRepository.findByAccountAndCurrencyId(account.getId(), currencyId);
        if(!amountCurrency.isPresent()) {
            logger.error("AMOUNT CURRENCY NOT PRESENT >>>>>>>> isBalanceSufficient :: AccountServiceImpl.java");
            return false;
        }

        if(Double.parseDouble(amount) >= 0 && Double.parseDouble(amountCurrency.get().getAmount()) > Double.parseDouble(amount))
            return true;

        return false;
    }

}
