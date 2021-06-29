package com.dusk.duskswap.withdrawal.controllers;

import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.mailing.models.Email;
import com.dusk.duskswap.commons.mailing.services.EmailService;
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
    private VerificationCodeService verificationCodeService;
    @Autowired
    private JwtUtils jwtUtils;

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/price")
    public ResponseEntity<SellPriceDto> calculatePrice(@RequestBody SellDto sellDto) {
        return sellService.calculateSale(sellDto);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/ask-confirmation", produces = "application/json")
    public ResponseEntity<Long> askConfirmation(@RequestBody SellDto sellDto) {

        // input checking
        if(
            sellDto == null ||
           (sellDto != null &&
                   (sellDto.getJwtToken() == null || (sellDto.getJwtToken() != null && sellDto.getJwtToken().isEmpty()))
           )
        )
            return ResponseEntity.badRequest().body(null);

        // user email extraction from jwt token
        String userEmail = jwtUtils.getEmailFromJwtToken(sellDto.getJwtToken());
        if(userEmail == null || (userEmail != null && userEmail.isEmpty()))
            return ResponseEntity.badRequest().body(null);

        // here we create the sale
        Sell sell = sellService.createSale(sellDto);
        // if null respond is returned, no need to send email, just return an error
        if(sell == null)
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);

        // if all's good, then we create a verification code and send it via email to user
        VerificationCode verificationCode = verificationCodeService.createWithdrawalCode(userEmail);
        Email email = new Email();
        email.setMessage(Integer.toString(verificationCode.getCode()));
        List<String> toAddresses = new ArrayList<>();
        toAddresses.add(userEmail);

        emailService.sendWithdrawalEmail(email);

        return ResponseEntity.ok(sell.getId());

    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/confirm", produces = "application/json")
    public ResponseEntity<Boolean> confirmSell(@RequestParam(value = "sellId") Long sellId) {
        return sellService.confirmSale(sellId);
    }


}
