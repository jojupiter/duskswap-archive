package com.dusk.duskswap.commons.controller;

import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.services.UtilitiesService;
import com.dusk.duskswap.commons.models.Currency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/currencies/create")
    public ResponseEntity<Currency> createCurrency(@RequestBody Currency currency) {
        return utilitiesService.createCurrency(currency);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/currencies/disable")
    public ResponseEntity<Boolean> disableCurrency(@RequestParam(name = "currencyId") Long currencyId) {
        return utilitiesService.enableCurrency(currencyId, false);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/currencies/activate")
    public ResponseEntity<Boolean> enableCurrency(@RequestParam(name = "currencyId") Long currencyId) {
        return utilitiesService.enableCurrency(currencyId, true);
    }

}
