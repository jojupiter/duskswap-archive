package com.dusk.duskswap.withdrawal.controllers;

import com.dusk.duskswap.account.models.AmountCurrency;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.mailing.models.Email;
import com.dusk.duskswap.commons.mailing.services.EmailService;
import com.dusk.duskswap.commons.miscellaneous.CodeErrors;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.VerificationCode;
import com.dusk.duskswap.commons.services.VerificationCodeService;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.services.UserService;
import com.dusk.duskswap.withdrawal.entityDto.SellDto;
import com.dusk.duskswap.withdrawal.entityDto.SellPage;
import com.dusk.duskswap.withdrawal.models.Sell;
import com.dusk.duskswap.withdrawal.services.SellService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/sell")
@Slf4j
public class SellController {

    @Autowired
    private SellService sellService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private JwtUtils jwtUtils;

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/confirm", produces = "application/json")
    public ResponseEntity<?> confirmation(@RequestBody SellDto sellDto) throws Exception{
        // input checking
        if(
            sellDto == null
        ) {
            log.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> confirmation :: SellController.java");
            return ResponseEntity.badRequest().body(null);
        }
        // >>>>> 1. getting the current authenticated user
        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 2. we check the account's balance
        ExchangeAccount account = accountService.getAccountByUser(user.get());
        if(account == null) {
            log.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT FOUND >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(CodeErrors.EXCHANGE_ACCOUNT_NOT_EXIST, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if(!accountService.isBalanceSufficient(account, sellDto.getFromCurrencyId(), sellDto.getAmount())) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(CodeErrors.INSUFFICIENT_AMOUNT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. here we verify if the provided code is correct
        if(!verificationCodeService.isCodeCorrect(user.get().getEmail(), sellDto.getCode(), DefaultProperties.VERIFICATION_WITHDRAWAL_SELL_PURPOSE)) {
            log.error("[" + new Date() + "] => CODE PROVIDED NOT CORRECT >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(CodeErrors.VERIFICATION_CODE_INCORRECT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 4. next we update the verification code in order the user won't send the same request twice (this is to avoid issues like debiting multiple time an account for a single operation)
        verificationCodeService.updateCode(user.get().getEmail(), DefaultProperties.VERIFICATION_WITHDRAWAL_SELL_PURPOSE);

        // >>>>> 5. then we create the sale
        Sell sell = sellService.createSale(sellDto, user.get(), account);
        if(sell == null) {
            log.error("[" + new Date() + "] => THE SELL OBJECT WASN'T CREATED >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 6. Finally we debit the user's account
        AmountCurrency amountCurrency = accountService.debitAccount(account, sell.getCurrency(), sellDto.getAmount());
        if(amountCurrency == null) {
            log.error("[" + new Date() + "] => THE ACCOUNT WASN'T DEBITED>>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(CodeErrors.UNABLE_TO_DEBIT_ACCOUNT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(true);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/ask-code", produces = "application/json")
    public ResponseEntity<Boolean> askCode() {
        // first we get the current authenticated user
        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> askCode :: SellController.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        VerificationCode code = verificationCodeService.createWithdrawalCode(user.get().getEmail());
        if(code == null) {
            log.error("[" + new Date() + "] => CODE NULL >>>>>>>> askCode :: SellController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Email email = new Email();
        email.setMessage(Integer.toString(code.getCode()));
        List<String> toAddresses = new ArrayList<>();
        toAddresses.add(user.get().getEmail());
        email.setTo(toAddresses);

        emailService.sendWithdrawalEmail(email);

        return ResponseEntity.ok(true);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/user-all", produces = "application/json")
    public ResponseEntity<?> getAllUserSales(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> etAllUserSales :: SellController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return sellService.getAllSales(user.get(), currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/user-all", produces = "application/json", params = "userId")
    public ResponseEntity<?> getAllUserSales(@RequestParam(name = "userId") Long userId,
                                             @RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        Optional<User> user = userService.getUser(userId);
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> etAllUserSales :: SellController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return sellService.getAllSales(user.get(), currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<SellPage> getAllSales(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return sellService.getAllSales(currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all-profits", produces = "application/json")
    public ResponseEntity<String> getAllSaleProfits() {
        return sellService.getAllSaleProfits();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all-profits-before", produces = "application/json")
    public ResponseEntity<String> getAllSaleProfitsBefore(@RequestParam(name = "date") @DateTimeFormat(pattern = "dd/MM/yyyy HH:ss") Date date) {
        return sellService.getAllSaleProfitsBefore(date);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all-profits-after", produces = "application/json")
    public ResponseEntity<String> getAllSaleProfitsAfter(@RequestParam(name = "date") @DateTimeFormat(pattern = "dd/MM/yyyy HH:ss") Date date) {
        return sellService.getAllSaleProfitsAfter(date);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all-profits-between", produces = "application/json")
    public ResponseEntity<String> getAllSaleProfitsBetween(@RequestParam(name = "startDate") @DateTimeFormat(pattern = "dd/MM/yyyy HH:ss") Date startDate,
                                                           @RequestParam(name = "endDate") @DateTimeFormat(pattern = "dd/MM/yyyy HH:ss") Date endDate) {
        return sellService.getAllSaleProfitsBetween(startDate, endDate);
    }

}
