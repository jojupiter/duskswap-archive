package com.dusk.duskswap.account.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RequestMapping("/accounts")
@RestController
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private JwtUtils jwtUtils;

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ExchangeAccount> createExchangeAccount(@RequestParam("token") String token) {
        String email = jwtUtils.getEmailFromJwtToken(token);
        return accountService.createExchangeAccount(email);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<ExchangeAccount> getExchangeAccount(@RequestParam("token") String token) {
        String email = jwtUtils.getEmailFromJwtToken(token);
        return accountService.getExchangeAccount(email);
    }


}
