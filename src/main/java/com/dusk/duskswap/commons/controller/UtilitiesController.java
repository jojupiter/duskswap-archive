package com.dusk.duskswap.commons.controller;

import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.services.UtilitiesService;
import com.dusk.duskswap.commons.models.Currency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/utilities")
public class UtilitiesController {

    @Autowired
    private UtilitiesService utilitiesService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-currencies")
    public List<Currency> getAllCurrencies() {
        return utilitiesService.getAllCurrencies();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/supported-currencies")
    public List<Currency> getAllSupportedCurrencies() {
        return utilitiesService.getAllSupportedCurrencies();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/supported-cryptos")
    public List<Currency> getAllSupportedCryptoCurrencies() {
        return utilitiesService.getAllSupportedCryptoCurrencies();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/supported-transaction-opts")
    public List<TransactionOption> getAllSupportedTransactionOptions() {
        return utilitiesService.getAllSupportedTransactionOptions();
    }

}
