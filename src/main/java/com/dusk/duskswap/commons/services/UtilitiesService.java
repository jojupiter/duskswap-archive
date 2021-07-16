package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.models.Level;
import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.usersManagement.models.User;

import java.util.List;
import java.util.Optional;

public interface UtilitiesService {

    List<Currency> getAllCurrencies();
    List<Currency> getAllSupportedCurrencies();
    List<Currency> getAllSupportedCryptoCurrencies();
    List<TransactionOption> getAllSupportedTransactionOptions();
    Optional<User> getCurrentUser();
    List<Level> getAllLevels();

}
