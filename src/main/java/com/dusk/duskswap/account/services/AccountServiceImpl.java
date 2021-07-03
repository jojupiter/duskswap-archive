package com.dusk.duskswap.account.services;

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
    public ResponseEntity<ExchangeAccount> createExchangeAccount(String userEmail) {

        // first, we check the input
        if((userEmail != null && userEmail.isEmpty()) || userEmail == null)
            return ResponseEntity.badRequest().body(null);

        // next, we check if the userEmail belongs to a real user
        Optional<User> user = userRepository.findByEmail(userEmail);
        if(!user.isPresent())
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);

        // here we check the status of user, if it's not activated we can't create an account
        if(!user.get().getStatus().getName().equals(DefaultProperties.STATUS_USER_ACTIVATED))
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);

        // then we proceed to the creation, by initializing the "based" currencies the user will use
        if(exchangeAccountRepository.existsByUser(user.get()))
            return ResponseEntity.ok(null);

        ExchangeAccount exchangeAccount = new ExchangeAccount();
        exchangeAccount.setUser(user.get());

        List<String> currencyStrings = new ArrayList<>();
        currencyStrings.add("USD");
        currencyStrings.add("BTC");
        currencyStrings.add("ETH");
        currencyStrings.add("LTC");

        List<Currency> currencies = currencyRepository.findByIsoIn(currencyStrings);

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
                    amountCurrency.setExchangeAccount(newExchangeAccount);
                    amountCurrency.setId(key);

                    amountCurrencies.add(amountCurrency);
                }
        );

        amountCurrencyRepository.saveAll(amountCurrencies);

        return ResponseEntity.ok(newExchangeAccount);
    }

    @Override
    public ResponseEntity<ExchangeAccount> getExchangeAccount(String userEmail) {
        // input checking
        if((userEmail!= null && userEmail.isEmpty()) || userEmail == null)
            return ResponseEntity.badRequest().body(null);

        // we find the corresponding user here
        Optional<User> user = userRepository.findByEmail(userEmail);
        if(!user.isPresent())
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);

         return ResponseEntity.ok(exchangeAccountRepository.findByUser(user.get()).get());
    }

    @Override
    public ExchangeAccount getAccountByUserEmail(String userEmail) {
        // input checking
        if((userEmail!= null && userEmail.isEmpty()) || userEmail == null)
            return null;

        // we find the corresponding user here
        Optional<User> user = userRepository.findByEmail(userEmail);
        if(!user.isPresent())
            return null;

        return exchangeAccountRepository.findByUser(user.get()).get();
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
    @Transactional
    public AmountCurrency fundAccount(ExchangeAccount account, Currency currency, String amount) {
        // inputs checking
        if(amount == null || currency == null || account == null)
            return null;

        Optional<AmountCurrency> amountCurrency = amountCurrencyRepository.findByAccountAndCurrencyId(account.getId(), currency.getId());
        if(!amountCurrency.isPresent())
            return null;

        Double currentAmount = Double.parseDouble(amountCurrency.get().getAmount());
        Double amountToAdd = Double.parseDouble(amount);

        if(amountToAdd < 0) // we can't add a negative amount
            return null;

        amountCurrency.get().setAmount(Double.toString(currentAmount + amountToAdd));

        return amountCurrencyRepository.save(amountCurrency.get());
    }

    @Override
    @Transactional
    public AmountCurrency debitAccount(ExchangeAccount account, Currency currency, String amount) {
        // input checking
        if(amount == null || currency == null || account == null)
            return null;

        Optional<AmountCurrency> amountCurrency = amountCurrencyRepository.findByAccountAndCurrencyId(account.getId(), currency.getId());
        if(!amountCurrency.isPresent())
            return null;

        Double currentAmount = Double.parseDouble(amountCurrency.get().getAmount());
        Double amountToRemove = Double.parseDouble(amount);

        if(amountToRemove < 0 || amountToRemove > currentAmount)
            return null;

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
