package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.models.Currency;

import java.util.List;

public interface UtilitiesService {

    List<Currency> getAllCurrencies();
    List<Currency> getAllSupportedCurrencies();
    List<TransactionOption> getAllSupportedTransactionOptions();

}
