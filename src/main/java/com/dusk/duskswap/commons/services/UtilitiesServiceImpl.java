package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.repositories.TransactionOptionRepository;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilitiesServiceImpl implements UtilitiesService {

    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private TransactionOptionRepository transactionOptionRepository;

    @Override
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    @Override
    public List<Currency> getAllSupportedCurrencies() {
        return currencyRepository.findByIsSupported(true);
    }

    @Override
    public List<TransactionOption> getAllSupportedTransactionOptions() {
        return transactionOptionRepository.findByIsSupported(true);
    }

}
