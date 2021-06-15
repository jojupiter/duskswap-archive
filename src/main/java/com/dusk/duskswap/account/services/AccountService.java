package com.dusk.duskswap.account.services;

import com.dusk.duskswap.account.models.AmountCurrency;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.shared.commons.models.Currency;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

public interface AccountService {

    ResponseEntity<ExchangeAccount> createExchangeAccount(String userEmail);

    ResponseEntity<ExchangeAccount> getExchangeAccount(String userEmail);
    ResponseEntity<List<ExchangeAccount>> getAllExchangeAccounts();
    ExchangeAccount getAccountById(Long accountId);

    List<AmountCurrency> fundAccount(ExchangeAccount account, Currency currency, String amount); // destined to be used by another service/controller outside this package
    List<AmountCurrency> debitAccount(ExchangeAccount account, Currency currency, String amount); // destined to be used by another service/controller outside this package

}
