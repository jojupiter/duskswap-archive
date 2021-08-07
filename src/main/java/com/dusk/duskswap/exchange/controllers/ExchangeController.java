package com.dusk.duskswap.exchange.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.commons.miscellaneous.CodeErrors;
import com.dusk.duskswap.commons.services.UtilitiesService;
import com.dusk.duskswap.exchange.entityDto.ExchangeDto;
import com.dusk.duskswap.exchange.entityDto.ExchangePage;
import com.dusk.duskswap.exchange.models.Exchange;
import com.dusk.duskswap.exchange.services.ExchangeService;
import com.dusk.duskswap.usersManagement.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/exchanges")
public class ExchangeController {

    @Autowired
    private ExchangeService exchangeService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UtilitiesService utilitiesService;
    private Logger logger = LoggerFactory.getLogger(ExchangeController.class);

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/user-all", produces = "application/json")
    public ResponseEntity<?> getAllUserExchanges(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        // >>>>>>> 1. we get the user
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserExchange :: ExchangeController.java");
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


    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<?> makeExchange(@RequestBody ExchangeDto dto) throws Exception {
        // >>>>>>> 1. we get the user
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> makeExchange :: ExchangeController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>>> 2. we get the exchange account
        ExchangeAccount exchangeAccount = accountService.getAccountByUser(user.get());
        if(exchangeAccount == null) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> makeExchange :: ExchangeController.java");
            return new ResponseEntity<>(CodeErrors.EXCHANGE_ACCOUNT_NOT_EXIST, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if(!accountService.isBalanceSufficient(exchangeAccount, dto.getFromCurrencyId(), dto.getFromAmount())) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> makeExchange :: ExchangeController.jav");
            return new ResponseEntity<>(CodeErrors.INSUFFICIENT_AMOUNT, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>>> 3. then we attempt to create the exchange
        Exchange createdExchange = exchangeService.makeExchange(dto, user.get(), exchangeAccount);

        if(createdExchange == null) {
            logger.error("[" + new Date() + "] => THE EXCHANGE WAS NOT CREATED >>>>>>>> makeExchange :: ExchangeController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>>> 4. when the exchange is created, we debit the account (from_amount)
        accountService.debitAccount(exchangeAccount, createdExchange.getFromCurrency(), dto.getFromAmount());

        // >>>>>> 5. then we fund the account (to_amount)
        accountService.fundAccount(exchangeAccount, createdExchange.getToCurrency(), createdExchange.getToAmount()/*dto.getToAmount()*/);

        return ResponseEntity.ok(true);

    }

}
