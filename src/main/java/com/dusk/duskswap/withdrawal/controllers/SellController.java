package com.dusk.duskswap.withdrawal.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.mailing.models.Email;
import com.dusk.duskswap.commons.mailing.services.EmailService;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.VerificationCode;
import com.dusk.duskswap.commons.services.VerificationCodeService;
import com.dusk.duskswap.withdrawal.entityDto.SellDto;
import com.dusk.duskswap.withdrawal.entityDto.SellPriceDto;
import com.dusk.duskswap.withdrawal.models.Sell;
import com.dusk.duskswap.withdrawal.services.SellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/sell")
public class SellController {

    @Autowired
    private SellService sellService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private JwtUtils jwtUtils;

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/price")
    public ResponseEntity<SellPriceDto> calculatePrice(@RequestBody SellDto sellDto) {
        return sellService.calculateSale(sellDto);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/confirm", produces = "application/json")
    public ResponseEntity<Boolean> confirmation(@RequestBody SellDto sellDto) {

        // input checking
        if(
            sellDto == null ||
           (sellDto != null &&
                   (sellDto.getJwtToken() == null || (sellDto.getJwtToken() != null && sellDto.getJwtToken().isEmpty()))
           )
        )
            return ResponseEntity.badRequest().body(false);

        // user email extraction from jwt token
        String userEmail = jwtUtils.getEmailFromJwtToken(sellDto.getJwtToken());
        if(userEmail == null || (userEmail != null && userEmail.isEmpty()))
            return ResponseEntity.badRequest().body(false);

        // balance checking
        ExchangeAccount account = accountService.getAccountByUserEmail(userEmail);
        if(!accountService.isBalanceSufficient(account, sellDto.getFromCurrencyId(), sellDto.getAmount()))
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);

        // here we verify if the provided code is correct and update it
        if(!verificationCodeService.isCodeCorrect(userEmail, sellDto.getCode(), DefaultProperties.VERIFICATION_WITHDRAWAL_SELL_PURPOSE))
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);

        verificationCodeService.updateCode(userEmail, DefaultProperties.VERIFICATION_WITHDRAWAL_SELL_PURPOSE);

        // then we create the sale
        Sell sell = sellService.createSale(sellDto);
        if(sell == null)
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);

        return ResponseEntity.ok(true);

    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/ask-code", produces = "application/json")
    public ResponseEntity<Boolean> askCode(@RequestParam(value = "email") String userEmail) {
        if(userEmail == null || (userEmail != null && userEmail.isEmpty()))
            return ResponseEntity.badRequest().body(false);

        VerificationCode code = verificationCodeService.createWithdrawalCode(userEmail);
        if(code == null)
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);

        Email email = new Email();
        email.setMessage(Integer.toString(code.getCode()));
        List<String> toAddresses = new ArrayList<>();
        toAddresses.add(userEmail);

        emailService.sendWithdrawalEmail(email);

        return ResponseEntity.ok(true);
    }


}
