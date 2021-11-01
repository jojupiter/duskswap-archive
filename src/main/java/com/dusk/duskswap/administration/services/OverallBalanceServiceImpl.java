package com.dusk.duskswap.administration.services;

import com.dusk.duskswap.administration.models.OverallBalance;
import com.dusk.duskswap.commons.miscellaneous.Codes;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.administration.repositories.OverallBalanceRepository;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OverallBalanceServiceImpl implements OverallBalanceService {

    @Autowired
    private OverallBalanceRepository overallBalanceRepository;
    @Autowired
    private CurrencyRepository currencyRepository;

    @Override
    public String getAvailableBalanceFor(Currency currency, int depositOrWithdrawal) {
        // input checking currency
        if(currency == null) {
            log.error("[" + new Date() + "] => CURRENCY NULL >>>>>>>> getAvailableBalanceFor :: OverallBalanceServiceImpl.java");
            return null;
        }

        // we apply repository method
        Optional<OverallBalance> overallBalance = overallBalanceRepository.findByCurrency(currency);
        if(!overallBalance.isPresent())
            return null;

        String balance = null;

        switch (depositOrWithdrawal) {
            case 0: balance = overallBalance.get().getDepositBalance();
                    break;
            case 1: balance = overallBalance.get().getWithdrawalBalance();
                    break;
        }

        return balance;
    }

    @Override
    public Optional<OverallBalance> getBalanceFor(Currency currency) {
        if(currency == null) {
            log.error("[" + new Date() + "] => CURRENCY NULL >>>>>>>> getBalanceFor :: OverallBalanceServiceImpl.java");
            return Optional.empty();
        }
        return overallBalanceRepository.findByCurrency(currency);
    }

    @Override
    public OverallBalance saveBalance(OverallBalance overallBalance) {
        if(overallBalance == null) {
            log.error("[" + new Date() + "] => OVER ALL BALANCE NULL >>>>>>>> saveBalance :: OverallBalanceServiceImpl.java");
            return null;
        }
        return overallBalanceRepository.save(overallBalance);
    }

    @Override
    public ResponseEntity<List<OverallBalance>> getAllBalances() {
        return ResponseEntity.ok(overallBalanceRepository.findAll());
    }

    @Override
    public ResponseEntity<?> increaseAmount(String amountToIncrease, Long currencyId, Integer depositOrWithdrawal) {
        // input checking
        if(
                amountToIncrease == null || (amountToIncrease != null && amountToIncrease.isEmpty()) ||
                currencyId == null ||
                depositOrWithdrawal == null
        ) {
            log.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> increaseAmount :: OverallBalanceServiceImpl.java" +
                    " ===== amountToIncrease = " + amountToIncrease + ", currencyId = " + currencyId + ", depositOrWithdrawal = " + depositOrWithdrawal);
                return ResponseEntity.badRequest().body(Codes.INPUT_ERROR_CODE);
        }

        // >>>>> 1. we get the currency
        Optional<Currency> currency = currencyRepository.findById(currencyId);
        if(!currency.isPresent()) {
            log.error("[" + new Date() + "] => CURRENCY NOT PRESENT >>>>>>>> increaseAmount :: OverallBalanceServiceImpl.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 2. we get balance for that particular currency
        Optional<OverallBalance> overallBalance = overallBalanceRepository.findByCurrency(currency.get());
        if(!overallBalance.isPresent()) {
            log.error("[" + new Date() + "] => BALANCE NOT PRESENT >>>>>>>> increaseAmount :: OverallBalanceServiceImpl.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. we increase the balance for that currency depending on whether it's for deposit or withdrawal
        double currentBalance = 0.0;
        double amountToAdd = Double.parseDouble(amountToIncrease);

        if(depositOrWithdrawal == 0) { // for deposit
            currentBalance = Double.parseDouble(overallBalance.get().getDepositBalance());
            overallBalance.get().setDepositBalance(Double.toString(currentBalance + amountToAdd));
        }
        if(depositOrWithdrawal == 1) { // for withdrawal
            currentBalance = Double.parseDouble(overallBalance.get().getWithdrawalBalance());
            overallBalance.get().setWithdrawalBalance(Double.toString(currentBalance + amountToAdd));
        }

        return ResponseEntity.ok(overallBalanceRepository.save(overallBalance.get()));
    }

    @Override
    public ResponseEntity<?> decreaseAmount(String amountToDecrease, Long currencyId, Integer depositOrWithdrawal) {
        // input checking
        if(
                amountToDecrease == null || (amountToDecrease != null && amountToDecrease.isEmpty()) ||
                currencyId == null ||
                depositOrWithdrawal == null
        ) {
            log.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> decreaseAmount :: OverallBalanceServiceImpl.java" +
                    " ===== amountToDecrease = " + amountToDecrease + ", currencyId = " + currencyId + ", depositOrWithdrawal = " + depositOrWithdrawal);
            return ResponseEntity.badRequest().body(null);
        }

        // >>>>> 1. we get the currency
        Optional<Currency> currency = currencyRepository.findById(currencyId);
        if(!currency.isPresent()) {
            log.error("[" + new Date() + "] => CURRENCY ACCOUNT NOT PRESENT >>>>>>>> decreaseAmount :: OverallBalanceServiceImpl.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 2. we get balance for that particular currency
        Optional<OverallBalance> overallBalance = overallBalanceRepository.findByCurrency(currency.get());
        if(!overallBalance.isPresent()) {
            log.error("[" + new Date() + "] => BALANCE NOT PRESENT >>>>>>>> increaseAmount :: OverallBalanceServiceImpl.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. we verify if there's enough amount to trigger the decrease
        double amountToSubtract = Double.parseDouble(amountToDecrease);

        if(
                ( Double.parseDouble(overallBalance.get().getDepositBalance()) - amountToSubtract < 0 && depositOrWithdrawal == 0) ||
                ( Double.parseDouble(overallBalance.get().getWithdrawalBalance()) - amountToSubtract < 0 && depositOrWithdrawal == 1)
        ) {
            log.error("[" + new Date() + "] => INSUFFICIENT BALANCE >>>>>>>> increaseAmount :: OverallBalanceServiceImpl.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 4. we increase the balance for that currency depending on whether it's for deposit or withdrawal
        double currentBalance = 0.0;

        if(depositOrWithdrawal == 0) { // for deposit
            currentBalance = Double.parseDouble(overallBalance.get().getDepositBalance());
            overallBalance.get().setDepositBalance(Double.toString(currentBalance - amountToSubtract));
        }
        if(depositOrWithdrawal == 1) { // for withdrawal
            currentBalance = Double.parseDouble(overallBalance.get().getWithdrawalBalance());
            overallBalance.get().setWithdrawalBalance(Double.toString(currentBalance - amountToSubtract));
        }

        return ResponseEntity.ok(overallBalanceRepository.save(overallBalance.get()));

    }

    @Override
    public OverallBalance increaseAmount(String amountToIncrease, Currency currency, Integer depositOrWithdrawal) {
        // input checking
        if(
                amountToIncrease == null || (amountToIncrease != null && amountToIncrease.isEmpty()) ||
                currency == null ||
                depositOrWithdrawal == null
        ) {
            log.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> increaseAmount :: OverallBalanceServiceImpl.java" +
                    " ===== amountToIncrease = " + amountToIncrease + ", currency = " + currency + ", depositOrWithdrawal = " + depositOrWithdrawal);
            return null;
        }

        // >>>>> 1. we get balance for that particular currency
        Optional<OverallBalance> overallBalance = overallBalanceRepository.findByCurrency(currency);
        if(!overallBalance.isPresent()) {
            log.error("[" + new Date() + "] => BALANCE NOT PRESENT >>>>>>>> increaseAmount :: OverallBalanceServiceImpl.java");
            return null;
        }

        // >>>>> 2. we increase the balance for that currency depending on whether it's for deposit or withdrawal
        double currentBalance = 0.0;
        double amountToAdd = Double.parseDouble(amountToIncrease);

        if(depositOrWithdrawal == 0) { // for deposit
            currentBalance = Double.parseDouble(overallBalance.get().getDepositBalance());
            overallBalance.get().setDepositBalance(Double.toString(currentBalance + amountToAdd));
        }
        if(depositOrWithdrawal == 1) { // for withdrawal
            currentBalance = Double.parseDouble(overallBalance.get().getWithdrawalBalance());
            overallBalance.get().setWithdrawalBalance(Double.toString(currentBalance + amountToAdd));
        }

        return overallBalanceRepository.save(overallBalance.get());
    }

    @Override
    public OverallBalance decreaseAmount(String amountToDecrease, Currency currency, Integer depositOrWithdrawal) {
        // input checking
        if(
                amountToDecrease == null || (amountToDecrease != null && amountToDecrease.isEmpty()) ||
                currency == null ||
                depositOrWithdrawal == null
        ) {
            log.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> decreaseAmount :: OverallBalanceServiceImpl.java" +
                    " ===== amountToDecrease = " + amountToDecrease + ", currency = " + currency + ", depositOrWithdrawal = " + depositOrWithdrawal);
            return null;
        }

        // >>>>> 1. we get balance for that particular currency
        Optional<OverallBalance> overallBalance = overallBalanceRepository.findByCurrency(currency);
        if(!overallBalance.isPresent()) {
            log.error("[" + new Date() + "] => BALANCE NOT PRESENT >>>>>>>> increaseAmount :: OverallBalanceServiceImpl.java");
            return null;
        }

        // >>>>> 2. we verify if there's enough amount to trigger the decrease
        double amountToSubtract = Double.parseDouble(amountToDecrease);

        if(
                ( Double.parseDouble(overallBalance.get().getDepositBalance()) - amountToSubtract < 0 && depositOrWithdrawal == 0) ||
                ( Double.parseDouble(overallBalance.get().getWithdrawalBalance()) - amountToSubtract < 0 && depositOrWithdrawal == 1)
        ) {
            log.error("[" + new Date() + "] => INSUFFICIENT BALANCE >>>>>>>> increaseAmount :: OverallBalanceServiceImpl.java");
            return null;
        }

        // >>>>> 3. we increase the balance for that currency depending on whether it's for deposit or withdrawal
        double currentBalance = 0.0;

        if(depositOrWithdrawal == 0) { // for deposit
            currentBalance = Double.parseDouble(overallBalance.get().getDepositBalance());
            overallBalance.get().setDepositBalance(Double.toString(currentBalance - amountToSubtract));
        }
        if(depositOrWithdrawal == 1) { // for withdrawal
            currentBalance = Double.parseDouble(overallBalance.get().getWithdrawalBalance());
            overallBalance.get().setWithdrawalBalance(Double.toString(currentBalance - amountToSubtract));
        }

        return overallBalanceRepository.save(overallBalance.get());
    }

    @Override
    public ResponseEntity<?> initializeBalances() {
        // >>>>> 1. we get all the supported currencies
        List<Currency> currencies = currencyRepository.findByIsSupported(true);

        // >>>>> 2. we create balance for every currencies
        List<OverallBalance> overallBalances = new ArrayList<>();

        currencies.forEach(
                currency -> {
                    if(!overallBalanceRepository.existsByCurrency(currency)) {
                        OverallBalance overallBalance = new OverallBalance();
                        overallBalance.setWithdrawalBalance("0.0");
                        overallBalance.setDepositBalance("0.0");
                        overallBalance.setTotalEarnings("0.0");
                        overallBalance.setCurrency(currency);

                        overallBalances.add(overallBalance);
                    }
                }
        );

        return ResponseEntity.ok(overallBalanceRepository.saveAll(overallBalances));
    }

    @Override
    public ResponseEntity<?> createBalanceFor(Long currencyId) {
        // input checking
        if(currencyId == null) {
            log.error("[" + new Date() + "] => INPUT NULL >>>>>>>> createBalanceFor :: OverallBalanceServiceImpl.java");
            return ResponseEntity.badRequest().body(Codes.INPUT_ERROR_CODE);
        }

        // >>>>> 1. we get the currency
        Optional<Currency> currency = currencyRepository.findById(currencyId);
        if(!currency.isPresent()) {
            log.error("[" + new Date() + "] => CURRENCY NOT PRESENT >>>>>>>> createBalanceFor :: OverallBalanceServiceImpl.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 2. we check if a balance for that currency already exists
        if(overallBalanceRepository.existsByCurrency(currency.get())) {
            log.error("[" + new Date() + "] => BALANCE ALREADY EXISTS >>>>>>>> createBalanceFor :: OverallBalanceServiceImpl.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. we proceed with the creation
        OverallBalance overallBalance = new OverallBalance();
        overallBalance.setWithdrawalBalance("0.0");
        overallBalance.setDepositBalance("0.0");
        overallBalance.setTotalEarnings("0.0");
        overallBalance.setCurrency(currency.get());

        return ResponseEntity.ok(overallBalanceRepository.save(overallBalance));
    }

    @Override
    public OverallBalance createBalanceFor(Currency currency) {
        if(currency == null) {
            log.error("[" + new Date() + "] => CURRENCY OBJECT NULL >>>>>>>> createBalanceFor :: OverallBalanceServiceImpl.java");
            return null;
        }

        // >>>>> 2. we check if a balance for that currency already exists
        if(overallBalanceRepository.existsByCurrency(currency)) {
            log.error("[" + new Date() + "] => BALANCE ALREADY EXISTS >>>>>>>> createBalanceFor :: OverallBalanceServiceImpl.java");
            return null;
        }

        // >>>>> 3. we proceed with the creation
        OverallBalance overallBalance = new OverallBalance();
        overallBalance.setWithdrawalBalance("0.0");
        overallBalance.setDepositBalance("0.0");
        overallBalance.setTotalEarnings("0.0");
        overallBalance.setCurrency(currency);

        return overallBalanceRepository.save(overallBalance);

    }

    @Override
    public ResponseEntity<?> updateBalanceFor(Long currencyId, String amount, Integer depositOrWithdrawal) {
        // input checking
        if(currencyId == null || depositOrWithdrawal == null) {
            log.error("[" + new Date() + "] => INPUT NULL >>>>>>>> updateBalanceFor :: OverallBalanceServiceImpl.java" +
                    " ===== currencyId = " + currencyId + ", depositOrWithdrawal = " + depositOrWithdrawal);
            return ResponseEntity.badRequest().body(Codes.INPUT_ERROR_CODE);
        }

        // >>>>> 1. we get the currency
        Optional<Currency> currency = currencyRepository.findById(currencyId);
        if(!currency.isPresent()) {
            log.error("[" + new Date() + "] => CURRENCY NOT PRESENT >>>>>>>> updateBalanceFor :: OverallBalanceServiceImpl.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 2. we get the balance
        Optional<OverallBalance> overallBalance = overallBalanceRepository.findByCurrency(currency.get());
        if(!overallBalance.isPresent()) {
            log.error("[" + new Date() + "] => BALANCE NOT PRESENT >>>>>>>> updateBalanceFor :: OverallBalanceServiceImpl.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. we proceed with the update
        if(depositOrWithdrawal == 0) {// deposit
            overallBalance.get().setDepositBalance(amount);
            return ResponseEntity.ok(overallBalanceRepository.save(overallBalance.get()));
        }
        if(depositOrWithdrawal == 1) { // withdrawal
            overallBalance.get().setWithdrawalBalance(amount);
            return ResponseEntity.ok(overallBalanceRepository.save(overallBalance.get()));
        }

        return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
    }

}
