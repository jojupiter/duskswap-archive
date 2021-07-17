package com.dusk.duskswap.exchange.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.commons.services.UtilitiesService;
import com.dusk.duskswap.exchange.entityDto.ExchangeDto;
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
@RequestMapping("/exchange")
public class ExchangeController {

    @Autowired
    private ExchangeService exchangeService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UtilitiesService utilitiesService;
    private Logger logger = LoggerFactory.getLogger(ExchangeController.class);

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<List<Exchange>> getAllUserExchanges() {
        // >>>>>>> 1. we get the user
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserExchange :: ExchangeController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>>>> 2. we then apply the corresponding method
        return exchangeService.getAllUserExchanges(user.get());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<List<Exchange>> getAllExchanges() {
        return exchangeService.getAllExchanges();
    }


    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<Boolean> makeExchange(@RequestBody ExchangeDto dto) {
        // >>>>>>> 1. we get the user
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> makeExchange :: ExchangeController.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>>> 2. we get the exchange accounut
        ExchangeAccount exchangeAccount = accountService.getAccountByUserEmail(user.get().getEmail());
        if(exchangeAccount == null) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> makeExchange :: ExchangeController.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>>> 3. then we attempt to create the exchange
        Exchange createdExchange = exchangeService.makeExchange(dto, user.get(), exchangeAccount);

        if(createdExchange == null) {
            logger.error("[" + new Date() + "] => THE EXCHANGE WAS NOT CREATED >>>>>>>> makeExchange :: ExchangeController.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>>> 4. when the exchange is created, we debit the account (from_amount)
        accountService.debitAccount(exchangeAccount, createdExchange.getFromCurrency(), dto.getFromAmount());

        // >>>>>> 5. then we fund the account (to_amount)
        accountService.fundAccount(exchangeAccount, createdExchange.getToCurrency(), dto.getToAmount());

        return ResponseEntity.ok(true);

    }

}
