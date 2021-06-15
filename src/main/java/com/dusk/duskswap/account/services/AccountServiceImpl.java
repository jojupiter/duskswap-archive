package com.dusk.duskswap.account.services;

import com.dusk.duskswap.account.models.AmountCurrency;
import com.dusk.duskswap.account.models.AmountCurrencyKey;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.AmountCurrencyRepository;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.shared.commons.repositories.CurrencyRepository;
import com.dusk.shared.commons.models.Currency;
import com.dusk.shared.usersManagement.models.User;
import com.dusk.shared.usersManagement.repositories.UserRepository;
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

    @Override
    public ResponseEntity<ExchangeAccount> createExchangeAccount(String userEmail) {

        // first, we check the input
        if((userEmail != null && userEmail.isEmpty()) || userEmail == null)
            return ResponseEntity.badRequest().body(null);

        // next, we check if the userEmail belongs to a real user
        Optional<User> user = userRepository.findByEmail(userEmail);
        if(!user.isPresent())
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
    public ResponseEntity<List<ExchangeAccount>> getAllExchangeAccounts() {
        return ResponseEntity.ok(exchangeAccountRepository.findAll());
    }

    @Override
    public ExchangeAccount getAccountById(Long accountId) {
        return exchangeAccountRepository.findById(accountId).get();
    }

    @Override
    @Transactional
    public List<AmountCurrency> fundAccount(ExchangeAccount account, Currency currency, String amount) {
        // inputs checking
        if(amount == null || currency == null || account == null)
            return null;

        account.getAmountCurrencies().forEach(
                amountCurrency -> {

                    if(amountCurrency.getCurrency().getId().equals(currency.getId())) {
                        Double currentAmount = Double.parseDouble(amountCurrency.getAmount());
                        Double addAmount = Double.parseDouble(amount);

                        Double newAmount = currentAmount + addAmount;
                        amountCurrency.setAmount(Double.toString(newAmount));
                    }
                }
        );

        return amountCurrencyRepository.saveAll(account.getAmountCurrencies());
    }

    @Override
    @Transactional
    public List<AmountCurrency> debitAccount(ExchangeAccount account, Currency currency, String amount) {
        if(amount == null || currency == null || account == null)
            return null;

        account.getAmountCurrencies().forEach(
                amountCurrency -> {

                    if(amountCurrency.getCurrency().getId().equals(currency.getId())) {
                        Double currentAmount = Double.parseDouble(amountCurrency.getAmount());
                        Double substractAmount = Double.parseDouble(amount);

                        Double newAmount = currentAmount - substractAmount;
                        amountCurrency.setAmount(Double.toString(newAmount));
                    }
                }
        );

        return amountCurrencyRepository.saveAll(account.getAmountCurrencies());
    }
}
