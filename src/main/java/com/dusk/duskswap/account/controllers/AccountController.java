package com.dusk.duskswap.account.controllers;

import com.dusk.duskswap.account.entityDto.CryptoBalance;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.miscellaneous.CodeErrors;
import com.dusk.duskswap.commons.services.UtilitiesService;
import com.dusk.duskswap.usersManagement.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin("*")
@RequestMapping("/accounts")
@RestController
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private UtilitiesService utilitiesService;
    private Logger logger = LoggerFactory.getLogger(AccountController.class);

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createExchangeAccount() {
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> createExchangeAccount :: AccountController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return accountService.createExchangeAccount(user.get());
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<?> getExchangeAccount() {
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getExchangeAccount :: AccountController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return accountService.getExchangeAccount(user.get());
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/balance/all")
    public ResponseEntity<?> getUserCryptoBalances() {
        // >>>>> 1. getting the current authenticated user
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getUserCryptoBalances :: AccountController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return accountService.getUserAccountBalance(user.get());
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/balance")
    public ResponseEntity<?> getUserCryptoBalance(@RequestParam("cryptoIso") String cryptoIso) {
        // >>>>> 1. getting the current authenticated user
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getUserCryptoBalance :: AccountController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return accountService.getUserCryptoBalance(user.get(), cryptoIso);
    }

}
