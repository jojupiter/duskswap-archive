package com.dusk.duskswap.transferring.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.commons.mailing.models.Email;
import com.dusk.duskswap.commons.mailing.services.EmailService;
import com.dusk.duskswap.commons.miscellaneous.CodeErrors;
import com.dusk.duskswap.commons.models.VerificationCode;
import com.dusk.duskswap.commons.repositories.PricingRepository;
import com.dusk.duskswap.commons.services.VerificationCodeService;
import com.dusk.duskswap.transferring.services.TransferService;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/transfers")
@Slf4j
public class TransferController {

    @Autowired
    private TransferService transferService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;
    @Autowired
    private PricingRepository pricingRepository;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private EmailService emailService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<?> getAllTransfers(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return transferService.getAllTransfers(currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/user-all", produces = "application/json", params = "accountId")
    public ResponseEntity<?> getAllUserTransfers(@RequestParam(name = "userId") Long userId,
                                                 @RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Optional<User> user = userService.getUser(userId);
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserTransfers :: TransferController.java");
            return ResponseEntity.badRequest().body(null);
        }

        ExchangeAccount account = accountService.getAccountByUser(user.get());
        if(account == null) {
            log.error("[" + new Date() + "] => CAN'T FIND ASSOCIATED EXCHANGE ACCOUNT FOR THIS USER >>>>>>>> getAllUserTransfers :: TransferController.java");
            return new ResponseEntity<>(CodeErrors.EXCHANGE_ACCOUNT_NOT_EXIST, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return transferService.getAllUserTransfers(account, currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/user-all", produces = "application/json")
    public ResponseEntity<?> getAllUserTransfers(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserTransfers :: TransferController.java");
            return ResponseEntity.badRequest().body(null);
        }

        ExchangeAccount account = accountService.getAccountByUser(user.get());
        if(account == null) {
            log.error("[" + new Date() + "] => CAN'T FIND ASSOCIATED EXCHANGE ACCOUNT FOR THIS USER >>>>>>>> getAllUserTransfers :: TransferController.java (User version)");
            return new ResponseEntity<>(CodeErrors.EXCHANGE_ACCOUNT_NOT_EXIST, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return transferService.getAllUserTransfers(account, currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/ask-code", produces = "application/json")
    public ResponseEntity<?> askCode() {
        // first we get the current authenticated user
        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> askCode :: TransferController.java");
            return new ResponseEntity<>(CodeErrors.JWT_TOKEN_INVALID, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // here we test if his email is null or empty
        if(user.isPresent() && (user.get().getEmail() == null || (user.get().getEmail() != null && user.get().getEmail().isEmpty()))) {
            log.error("[" + new Date() + "] => USER EMAIL NULL >>>>>>>> askCode :: TransferController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // if email is not null, then we create the verification code
        VerificationCode code = verificationCodeService.createTransferCode(user.get().getEmail());
        if(code == null) {
            log.error("[" + new Date() + "] => CODE NULL >>>>>>>> askCode :: TransferController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // after that, we send an email with the code
        Email email = new Email();
        email.setMessage(Integer.toString(code.getCode()));
        List<String> toAddresses = new ArrayList<>();
        toAddresses.add(user.get().getEmail());
        email.setTo(toAddresses);

        emailService.sendTransferEmail(email);

        return ResponseEntity.ok(true);

    }

}
