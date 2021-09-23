package com.dusk.duskswap.exchange.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.administration.services.OverallBalanceService;
import com.dusk.duskswap.commons.miscellaneous.CodeErrors;
import com.dusk.duskswap.exchange.entityDto.ExchangeDto;
import com.dusk.duskswap.exchange.entityDto.ExchangePage;
import com.dusk.duskswap.exchange.models.Exchange;
import com.dusk.duskswap.exchange.services.ExchangeService;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/exchanges")
@Slf4j
public class ExchangeController {

    @Autowired
    private ExchangeService exchangeService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;
    @Autowired
    private OverallBalanceService overallBalanceService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/user-all", produces = "application/json")
    public ResponseEntity<?> getAllUserExchanges(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        // >>>>>>> 1. we get the user
        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserExchange :: ExchangeController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>>>> 2. we then apply the corresponding method
        return exchangeService.getAllUserExchanges(user.get(), currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/user-all", produces = "application/json", params = "userId")
    public ResponseEntity<?> getAllUserExchanges(@RequestParam(name = "userId") Long userId,
                                                 @RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        // >>>>>>> 1. we get the user
        Optional<User> user = userService.getUser(userId);
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserExchange :: ExchangeController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>>>> 2. we then apply the corresponding method
        return exchangeService.getAllUserExchanges(user.get(), currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<ExchangePage> getAllExchanges(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return exchangeService.getAllExchanges(currentPage, pageSize);
    }


    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<?> makeExchange(@RequestBody ExchangeDto dto) throws Exception {
        // >>>>>>> 1. we get the user
        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> makeExchange :: ExchangeController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>>> 2. we get the exchange account
        ExchangeAccount exchangeAccount = accountService.getAccountByUser(user.get());
        if(exchangeAccount == null) {
            log.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> makeExchange :: ExchangeController.java");
            return new ResponseEntity<>(CodeErrors.EXCHANGE_ACCOUNT_NOT_EXIST, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if(!accountService.isBalanceSufficient(exchangeAccount, dto.getFromCurrencyId(), dto.getFromAmount())) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> makeExchange :: ExchangeController.jav");
            return new ResponseEntity<>(CodeErrors.INSUFFICIENT_AMOUNT, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>>> 3. then we attempt to create the exchange
        Exchange createdExchange = exchangeService.makeExchange(dto, user.get(), exchangeAccount);

        if(createdExchange == null) {
            log.error("[" + new Date() + "] => THE EXCHANGE WAS NOT CREATED >>>>>>>> makeExchange :: ExchangeController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>>> 4. when the exchange is created, we debit the account (from_amount) + also debit the overall amount for that currency
        accountService.debitAccount(exchangeAccount, createdExchange.getFromCurrency(), dto.getFromAmount());
        overallBalanceService.decreaseAmount(dto.getFromAmount(), createdExchange.getFromCurrency(), 0);

        // >>>>>> 5. then we fund the account (to_amount)
        accountService.fundAccount(exchangeAccount, createdExchange.getToCurrency(), createdExchange.getToAmount()/*dto.getToAmount()*/);
        overallBalanceService.increaseAmount(createdExchange.getToAmount(),  createdExchange.getToCurrency(), 0);

        return ResponseEntity.ok(true);

    }

}
