package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.models.Level;
import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.usersManagement.models.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface UtilitiesService {

    List<Currency> getAllCurrencies();
    List<Currency> getAllSupportedCurrencies();
    List<Currency> getAllSupportedCryptoCurrencies();
    List<TransactionOption> getAllSupportedTransactionOptions();
    ResponseEntity<Boolean> enableCurrency(Long currencyId, Boolean isSupported);
    ResponseEntity<Currency> createCurrency(Currency currency);

    Optional<User> getCurrentUser();

    ResponseEntity<Level> createLevel(Level level);
    ResponseEntity<List<Level>> getAllLevels();
    ResponseEntity<Level> updateLevel(Long levelId, Level newLevel);
    ResponseEntity<Boolean> deleteLevel(Long levelId);

}
