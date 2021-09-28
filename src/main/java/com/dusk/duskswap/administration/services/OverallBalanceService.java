package com.dusk.duskswap.administration.services;

import com.dusk.duskswap.administration.models.OverallBalance;
import com.dusk.duskswap.commons.models.Currency;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface OverallBalanceService {

    String getAvailableBalanceFor(Currency currency, int depositOrWithdrawal); // 0: deposit, 1: withdrawal
    Optional<OverallBalance> getBalanceFor(Currency currency);
    OverallBalance saveBalance(OverallBalance overallBalance);
    ResponseEntity<List<OverallBalance>> getAllBalances();
    ResponseEntity<?> increaseAmount(String amountToIncrease, Long currencyId, Integer depositOrWithdrawal); // 0: deposit, 1: withdrawal
    ResponseEntity<?> decreaseAmount(String amountToDecrease, Long currencyId, Integer depositOrWithdrawal); // 0: deposit, 1: withdrawal
    OverallBalance increaseAmount(String amountToIncrease, Currency currency, Integer depositOrWithdrawal);
    OverallBalance decreaseAmount(String amountToIncrease, Currency currency, Integer depositOrWithdrawal);
    ResponseEntity<?> initializeBalances();
    ResponseEntity<?> createBalanceFor(Long currencyId);
    OverallBalance createBalanceFor(Currency currency);
    ResponseEntity<?> updateBalanceFor(Long currencyId, String amount, Integer depositOrWithdrawal); // 0: deposit, 1: withdrawal

}
