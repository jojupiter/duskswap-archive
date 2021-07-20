package com.dusk.duskswap.account.services;

import com.dusk.duskswap.account.entityDto.CryptoBalance;
import com.dusk.duskswap.account.models.AmountCurrency;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.usersManagement.models.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AccountService {

    ResponseEntity<ExchangeAccount> createExchangeAccount(String userEmail); // TODO: put user object instead of user email
    ResponseEntity<ExchangeAccount> getExchangeAccount(String userEmail); // TODO: put user object instead of user email
    ResponseEntity<List<ExchangeAccount>> getAllExchangeAccounts();
    ExchangeAccount getAccountById(Long accountId);
    ExchangeAccount getAccountByUserEmail(String userEmail); // TODO: put user object instead of user email

    ResponseEntity<List<CryptoBalance>> getUserAccountBalance(User user);
    ResponseEntity<CryptoBalance> getUserCryptoBalance(User user, String cryptoIso);

    AmountCurrency fundAccount(ExchangeAccount account, Currency currency, String amount); // destined to be used by another service/controller outside this package
    AmountCurrency debitAccount(ExchangeAccount account, Currency currency, String amount); // destined to be used by another service/controller outside this package

    boolean isBalanceSufficient(ExchangeAccount account, Long currencyId, String amount);
}
