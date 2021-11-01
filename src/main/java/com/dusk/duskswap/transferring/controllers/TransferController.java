package com.dusk.duskswap.transferring.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.administration.models.OverallBalance;
import com.dusk.duskswap.administration.services.OverallBalanceService;
import com.dusk.duskswap.commons.mailing.models.Email;
import com.dusk.duskswap.commons.mailing.services.EmailService;
import com.dusk.duskswap.commons.miscellaneous.Codes;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.VerificationCode;
import com.dusk.duskswap.commons.repositories.PricingRepository;
import com.dusk.duskswap.commons.services.VerificationCodeService;
import com.dusk.duskswap.transferring.entityDtos.TransferDto;
import com.dusk.duskswap.transferring.entityDtos.TransferResponse;
import com.dusk.duskswap.transferring.models.Transfer;
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
    @Autowired
    private OverallBalanceService overallBalanceService;

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
            return ResponseEntity.badRequest().body(Codes.USER_NOT_FOUND);
        }

        ExchangeAccount account = accountService.getAccountByUser(user.get());
        if(account == null) {
            log.error("[" + new Date() + "] => CAN'T FIND ASSOCIATED EXCHANGE ACCOUNT FOR THIS USER >>>>>>>> getAllUserTransfers :: TransferController.java");
            return new ResponseEntity<>(Codes.EXCHANGE_ACCOUNT_NOT_EXIST, HttpStatus.UNPROCESSABLE_ENTITY);
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
            return ResponseEntity.badRequest().body(Codes.USER_NOT_FOUND);
        }

        ExchangeAccount account = accountService.getAccountByUser(user.get());
        if(account == null) {
            log.error("[" + new Date() + "] => CAN'T FIND ASSOCIATED EXCHANGE ACCOUNT FOR THIS USER >>>>>>>> getAllUserTransfers :: TransferController.java (User version)");
            return new ResponseEntity<>(Codes.EXCHANGE_ACCOUNT_NOT_EXIST, HttpStatus.UNPROCESSABLE_ENTITY);
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
            return new ResponseEntity<>(Codes.USER_NOT_FOUND, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // here we test if his email is null or empty
        if(user.isPresent() && (user.get().getEmail() == null || (user.get().getEmail() != null && user.get().getEmail().isEmpty()))) {
            log.error("[" + new Date() + "] => USER EMAIL NULL >>>>>>>> askCode :: TransferController.java");
            return new ResponseEntity<>(Codes.EMAIL_NOT_EXISTING, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // if email is not null, then we create the verification code
        VerificationCode code = verificationCodeService.createTransferCode(user.get().getEmail());
        if(code == null) {
            log.error("[" + new Date() + "] => CODE NULL >>>>>>>> askCode :: TransferController.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
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

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/confirm", produces = "application/json")
    public ResponseEntity<?> makeTransfer(@RequestBody TransferDto dto) throws Exception {
        // input checking
        if(
                dto == null ||
                (
                     dto != null &&
                     (
                             dto.getAmount() == null || (dto.getAmount() != null && dto.getAmount().isEmpty()) ||
                             dto.getCurrencyId() == null ||
                             dto.getRecipientUserId() == null
                     )
                )
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT >>>>>>>> makeTransfer :: TransferController.java " +
                    "====== dto = " + dto);
            return ResponseEntity.badRequest().body(Codes.INPUT_ERROR_CODE);
        }

        // >>>>> 1. we get the current logged in user and the recipient
        Optional<User> sender = userService.getCurrentUser();
        if(!sender.isPresent()) {
            log.error("[" + new Date() + "] => SENDER NOT PRESENT >>>>>>>> makeTransfer :: TransferController.java");
            return new ResponseEntity<>(Codes.USER_NOT_FOUND, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Optional<User> recipient = userService.getUser(dto.getRecipientUserId());
        if(!recipient.isPresent()) {
            log.error("[" + new Date() + "] => RECIPIENT NOT PRESENT >>>>>>>> makeTransfer :: TransferController.java");
            return new ResponseEntity<>(Codes.USER_NOT_FOUND, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(recipient.get().getId() == sender.get().getId()) {
            log.error("[" + new Date() + "] => RECIPIENT AND SENDER ARE SAME >>>>>>>> makeTransfer :: TransferController.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 2. check the code
        if(!verificationCodeService.isCodeCorrect(sender.get().getEmail(), dto.getCode(), DefaultProperties.VERIFICATION_TRANSFER_PURPOSE)) {
            log.error("[" + new Date() + "] => CODE PROVIDED INCORRECT >>>>>>>> makeTransfer :: TransferController.java");
            return new ResponseEntity<>(Codes.VERIFICATION_CODE_INCORRECT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. then we get their account
        ExchangeAccount senderAccount = accountService.getAccountByUser(sender.get());
        if(senderAccount == null) {
            log.error("[" + new Date() + "] => SENDER ACCOUNT NOT PRESENT >>>>>>>> makeTransfer :: TransferController.java");
            return new ResponseEntity<>(Codes.EXCHANGE_ACCOUNT_NOT_EXIST, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        ExchangeAccount recipientAccount = accountService.getAccountByUser(recipient.get());
        if(recipientAccount == null) {
            log.error("[" + new Date() + "] => RECIPIENT ACCOUNT NOT PRESENT >>>>>>>> makeTransfer :: TransferController.java");
            return new ResponseEntity<>(Codes.EXCHANGE_ACCOUNT_NOT_EXIST, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 4. we can now create the transfer object without saving it in DB
        Transfer transfer = transferService.createTransfer(sender.get(), senderAccount, recipientAccount, dto.getCurrencyId(), dto.getAmount());
        if(transfer == null) {
            log.error("[" + new Date() + "] => CAN'T MAKE THE TRANSFER >>>>>>>> makeTransfer :: TransferController.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 5. we check if the sender and duskswap have enough balance to execute the transfer
        Double totalTransferAmount = Double.parseDouble(dto.getAmount()) + Double.parseDouble(transfer.getFees());
        if(!accountService.isBalanceSufficient(senderAccount, dto.getCurrencyId(), Double.toString(totalTransferAmount))) {
            log.error("[" + new Date() + "] => SENDER " + sender.get().getEmail() + " HAS INSUFFICIENT BALANCE >>>>>>>> makeTransfer :: TransferController.java");
            return new ResponseEntity<>(Codes.INSUFFICIENT_BALANCE_USER, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<OverallBalance> overallBalance = overallBalanceService.getBalanceFor(transfer.getCurrency());
        if(!overallBalance.isPresent()) {
            log.error("[" + new Date() + "] => OVERALL BALANCE NOT PRESENT FOR CURRENCY :" + transfer.getCurrency().getIso() + ">>>>>>>> makeTransfer :: TransferController.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(Double.parseDouble(overallBalance.get().getDepositBalance()) <= totalTransferAmount) {
            log.error("[" + new Date() + "] => OVERALL BALANCE NOT SUFFICIENT FOR CURRENCY :" + transfer.getCurrency().getIso() + " (" +
                    " balance = " + overallBalance.get().getDepositBalance() + ", totalTransferAmount = " + totalTransferAmount + ")>>>>>>>> makeTransfer :: TransferController.java");
            return new ResponseEntity<>(Codes.INSUFFICIENT_BALANCE_DUSKSWAP, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 6. we save the transfer
        Transfer savedTransfer = transferService.saveTransfer(transfer);

        // >>>>> 7. we debit and fund accounts
        accountService.debitAccount(senderAccount, savedTransfer.getCurrency(), Double.toString(totalTransferAmount));
        accountService.fundAccount(recipientAccount, savedTransfer.getCurrency(), transfer.getAmount());

        if(Double.parseDouble(transfer.getFees()) > 0.0) { // we take fees in overall deposit balance and put it in total earnings
            Double newTotalEarningsDusk = Double.parseDouble(overallBalance.get().getTotalEarnings()) + Double.parseDouble(transfer.getFees());
            Double newDepositBalance = Double.parseDouble(overallBalance.get().getDepositBalance()) - Double.parseDouble(transfer.getFees());
            overallBalance.get().setTotalEarnings(Double.toString(newTotalEarningsDusk));
            overallBalance.get().setDepositBalance(Double.toString(newDepositBalance));
            overallBalanceService.saveBalance(overallBalance.get());
        }

        // >>>>> 8. finally we return the transfer response object
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setAmount(transfer.getAmount());
        transferResponse.setRecipientEmail(recipient.get().getEmail());
        transferResponse.setFees(transfer.getFees());

        return ResponseEntity.ok(transferResponse);

    }

}
