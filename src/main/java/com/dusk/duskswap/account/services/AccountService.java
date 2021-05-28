package com.dusk.duskswap.account.services;

import com.dusk.duskswap.account.models.ExchangeAccount;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AccountService {

    ResponseEntity<ExchangeAccount> createExchangeAccount(String userEmail);

    ResponseEntity<ExchangeAccount> getExchangeAccount(String emailUser);
    ResponseEntity<List<ExchangeAccount>> getAllExchangeAccounts();

    ExchangeAccount fundAccount(Double amount, String currency, String emailUser); // destined to be used by another service/controller outside this package
    ExchangeAccount debitAccount(Double amount, String currency, String emailUser); // destined to be used by another service/controller outside this package

}
