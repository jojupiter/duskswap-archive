package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.models.*;
import com.dusk.duskswap.usersManagement.models.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface UtilitiesService {

    Optional<Currency> getCurrencyById(Long currencyId);
    List<Currency> getAllCurrencies();
    List<Currency> getAllSupportedCurrencies();
    List<Currency> getAllSupportedCryptoCurrencies();
    List<TransactionOption> getAllSupportedTransactionOptions();
    ResponseEntity<Boolean> enableCurrency(Long currencyId, Boolean isSupported);
    ResponseEntity<Currency> createCurrency(Currency currency);
    ResponseEntity<Currency> updateCurrency(Long currencyId, Currency newCurrency);

    ResponseEntity<Level> createLevel(Level level);
    ResponseEntity<List<Level>> getAllLevels();
    ResponseEntity<Level> updateLevel(Long levelId, Level newLevel);
    ResponseEntity<Boolean> deleteLevel(Long levelId);

}
