package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.account.models.AmountCurrency;
import com.dusk.duskswap.account.models.AmountCurrencyKey;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.AmountCurrencyRepository;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.Level;
import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.repositories.LevelRepository;
import com.dusk.duskswap.commons.repositories.TransactionOptionRepository;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.models.UserDetailsImpl;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UtilitiesServiceImpl implements UtilitiesService {

    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private TransactionOptionRepository transactionOptionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LevelRepository levelRepository;
    @Autowired
    private AmountCurrencyRepository amountCurrencyRepository;
    @Autowired
    private ExchangeAccountRepository exchangeAccountRepository;
    private Logger logger = LoggerFactory.getLogger(UtilitiesServiceImpl.class);

    @Override
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    @Override
    public List<Currency> getAllSupportedCurrencies() {
        return currencyRepository.findByIsSupported(true);
    }

    @Override
    public List<Currency> getAllSupportedCryptoCurrencies() {
        return currencyRepository.findByTypeAndIsSupported(DefaultProperties.CURRENCY_TYPE_CRYPTO, true);
    }

    @Override
    public List<TransactionOption> getAllSupportedTransactionOptions() {
        return transactionOptionRepository.findByIsSupported(true);
    }

    @Override
    public ResponseEntity<Boolean> enableCurrency(Long currencyId, Boolean isSupported) {
        // input checking
        if(currencyId == null || isSupported == null) {
            logger.error("[" + new Date() + "] => INPUT NULL >>>>>>>> deactivateCurrency :: UtilitiesServiceImpl.java" +
                    " ==== currencyId = " + currencyId + ", isSupported = " + isSupported);
            return ResponseEntity.badRequest().body(false);
        }
        // getting currency
        Optional<Currency> currency = currencyRepository.findById(currencyId);
        if(!currency.isPresent()) {
            logger.error("[" + new Date() + "] => CURRENCY NOT PRESENT >>>>>>>> deactivateCurrency :: UtilitiesServiceImpl.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        currency.get().setIsSupported(isSupported);
        Currency currency1 = currencyRepository.save(currency.get());
        if(currency1 == null) {
            logger.error("[" + new Date() + "] => UNABLE TO SAVE CURRENCY >>>>>>>> deactivateCurrency :: UtilitiesServiceImpl.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return ResponseEntity.ok(true);
    }

    @Override
    public ResponseEntity<Currency> createCurrency(Currency currency) {
        if(
                currency == null ||
                (currency != null && (
                            currency.getIso() == null || (currency.getIso() != null && currency.getIso().isEmpty()) ||
                            currency.getName() == null || (currency.getName() != null && currency.getName().isEmpty())
                        )
                )
        ) {
            logger.error("[" + new Date() + "] => INPUT NULL (currency="+ currency + ") >>>>>>>> deactivateCurrency :: UtilitiesServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        // by default, we set the attribute is_supported to true
        if(currency.getIsSupported() == null)
            currency.setIsSupported(true);

        Currency createdCurrency = currencyRepository.save(currency);

        // after that, we have to add the corresponding amount currency for all th users
        List<ExchangeAccount> accounts = exchangeAccountRepository.findAll();
        List<AmountCurrency> amountCurrencies = new ArrayList<>();
        if(accounts != null && !accounts.isEmpty()) {
            for(ExchangeAccount account: accounts) {
                AmountCurrencyKey key = new AmountCurrencyKey();
                key.setCurrencyId(currency.getId());
                key.setExchangeAccountId(account.getId());

                AmountCurrency amountCurrency = new AmountCurrency();
                amountCurrency.setId(key);
                amountCurrency.setAmount(Double.toString(0.0));
                amountCurrency.setCurrency(createdCurrency);
                amountCurrency.setExchangeAccount(account);
                amountCurrencies.add(amountCurrency);
            }
            amountCurrencyRepository.saveAll(amountCurrencies);
        }

        return ResponseEntity.ok(createdCurrency);
    }

    // =======================================================================================
    @Override
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken || authentication == null)
            return Optional.empty();
        Long userId = ((UserDetailsImpl)authentication.getPrincipal()).getId();
        if(userId == null)
            return Optional.empty();
        return userRepository.findById(userId);
    }

    // =====================================================================================
    @Override
    public ResponseEntity<List<Level>> getAllLevels() {
        return ResponseEntity.ok(levelRepository.findAll());
    }

    @Override
    public ResponseEntity<Level> createLevel(Level level) {
        if(level == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(levelRepository.save(level));
    }


    @Override
    public ResponseEntity<Level> updateLevel(Long levelId, Level newLevel) {
        // input checking
        if(levelId == null || newLevel == null) {
            logger.error("[" + new Date() + "] => INPUT NULL >>>>>>>> updateLevel :: UtilitiesServiceImpl.java" +
                    " ==== levelId = " + levelId + ", newLevel = " + newLevel);
            return ResponseEntity.badRequest().body(null);
        }
        // we get the corresponding level
        Optional<Level> level = levelRepository.findById(levelId);
        if(!level.isPresent()) {
            logger.error("[" + new Date() + "] => LEVEL NOT PRESENT >>>>>>>> updateLevel :: UtilitiesServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // then we proceed to update
        if(newLevel.getIso() != null && !newLevel.getIso().isEmpty())
            level.get().setIso(newLevel.getIso());
        if(newLevel.getName() != null && !newLevel.getName().isEmpty())
            level.get().setName(newLevel.getName());

        return ResponseEntity.ok(levelRepository.save(level.get()));
    }

    @Override
    public ResponseEntity<Boolean> deleteLevel(Long levelId) {
        // input checking
        if(levelId == null) {
            logger.error("[" + new Date() + "] => INPUT NULL (levelId) >>>>>>>> deleteLevel :: UtilitiesServiceImpl.java");
            return ResponseEntity.badRequest().body(false);
        }
        levelRepository.deleteById(levelId);
        return ResponseEntity.ok(true);
    }
}
