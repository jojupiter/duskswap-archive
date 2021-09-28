package com.dusk.duskswap.withdrawal.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.administration.models.OverallBalance;
import com.dusk.duskswap.administration.services.OverallBalanceService;
import com.dusk.duskswap.commons.mailing.models.Email;
import com.dusk.duskswap.commons.mailing.services.EmailService;
import com.dusk.duskswap.commons.miscellaneous.CodeErrors;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.TransactionBlock;
import com.dusk.duskswap.commons.models.VerificationCode;
import com.dusk.duskswap.commons.models.WalletTransaction;
import com.dusk.duskswap.commons.models.WalletTransactionDestination;
import com.dusk.duskswap.commons.services.InvoiceService;
import com.dusk.duskswap.commons.services.VerificationCodeService;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.services.UserService;
import com.dusk.duskswap.withdrawal.entityDto.WithdrawalDto;
import com.dusk.duskswap.withdrawal.entityDto.WithdrawalPage;
import com.dusk.duskswap.withdrawal.models.Withdrawal;
import com.dusk.duskswap.withdrawal.services.WithdrawalService;
import com.dusk.externalAPIs.apiInterfaces.interfaces.BlockExplorerOperations;
import com.dusk.externalAPIs.apiInterfaces.models.TransactionInfos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/withdrawals")
@Slf4j
public class WithdrawalController {

    @Autowired
    private WithdrawalService withdrawalService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private UserService userService;
    @Autowired
    private BlockExplorerOperations blockExplorerOperations;
    @Autowired
    private OverallBalanceService overallBalanceService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<WithdrawalPage> getAllWithdrawals(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return withdrawalService.getAllWithdrawals(currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/user-all", produces = "application/json")
    public ResponseEntity<WithdrawalPage> getAllUserWithdrawals(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserWithdrawals :: WithdrawalController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return withdrawalService.getAllUserWithdrawals(user.get(), currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/user-all", produces = "application/json", params = "userId")
    public ResponseEntity<WithdrawalPage> getAllUserWithdrawals(@RequestParam(name = "userId") Long userId,
                                                                @RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        Optional<User> user = userService.getUser(userId);
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserWithdrawals :: WithdrawalController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return withdrawalService.getAllUserWithdrawals(user.get(), currentPage, pageSize);
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/ask-code", produces = "application/json")
    public ResponseEntity<Boolean> askCode() {
        // >>>>> 1. first we get the current authenticated user
        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> askCode :: WithdrawalController.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>> 2. here we test if his email is null or empty
        if(user.isPresent() && (user.get().getEmail() == null || (user.get().getEmail() != null && user.get().getEmail().isEmpty()))) {
            log.error("[" + new Date() + "] => USER EMAIL NULL >>>>>>>> askCode :: WithdrawalController.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. if email is not null, then we create the verification code
        VerificationCode code = verificationCodeService.createWithdrawalCode(user.get().getEmail());
        if(code == null) {
            log.error("[" + new Date() + "] => CODE NULL >>>>>>>> askCode :: WithdrawalController.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 4. after that, we send an email with the code
        Email email = new Email();
        email.setMessage(Integer.toString(code.getCode()));
        List<String> toAddresses = new ArrayList<>();
        toAddresses.add(user.get().getEmail());
        email.setTo(toAddresses);

        emailService.sendWithdrawalEmail(email);

        return ResponseEntity.ok(true);

    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/confirm", produces = "application/json")
    @Transactional
    public ResponseEntity<?> confirm(@RequestBody WithdrawalDto dto) throws Exception { // returns the hash of the transaction
        // input checking
        if(dto == null || (dto != null && dto.getCode() == null)) {
            log.error("[" + new Date() + "] => CODE NULL >>>>>>>> confirm :: WithdrawalController.java");
            return ResponseEntity.badRequest().body(null);
        }

        // =============================== Getting necessary elements =============================
        // >>>>> 1. current authenticated user
        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> confirm :: WithdrawalController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // here we check if his email is null or empty
        if(user.isPresent() && (user.get().getEmail() == null || (user.get().getEmail() != null && user.get().getEmail().isEmpty()))) {
            log.error("[" + new Date() + "] => USER EMAIL NULL >>>>>>>> confirm :: WithdrawalController.java");
            return new ResponseEntity<>(CodeErrors.EMAIL_NOT_EXIST, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>> 2. the user's exchange account
        ExchangeAccount account = accountService.getAccountByUser(user.get());

        // >>>>> 3. first we check if verification code is correct
        if(!verificationCodeService.isCodeCorrect(user.get().getEmail(), dto.getCode(), DefaultProperties.VERIFICATION_WITHDRAWAL_SELL_PURPOSE)) {
            log.error("[" + new Date() + "] => VERIFICATION CODE INCORRECT >>>>>>>> confirm :: WithdrawalController.java");
            return new ResponseEntity<>(CodeErrors.VERIFICATION_CODE_INCORRECT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // =============================== creation of the withdrawal + balances checking =================================
        // >>>>> 4. then we create the withdrawal
        Withdrawal withdrawal = withdrawalService.createWithdrawal(dto, user.get(), account);
        if(withdrawal == null) {
            log.error("[" + new Date() + "] => WITHDRAWAL NULL >>>>>>>> confirm :: WithdrawalController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 5. we get the overall balance for this currency
        Optional<OverallBalance> overallBalance = overallBalanceService.getBalanceFor(withdrawal.getCurrency());

        // >>>>> 6. we get an estimation of fee rates and network fees that will be used in the transaction
        // (max between what the Api send to us and the default max value we set)
        Double estimatedFeeRate = blockExplorerOperations.getEstimatedFeeRate(withdrawal.getCurrency().getIso());
        Double estimatedFeesGivenByAPI = blockExplorerOperations.getEstimatedFees(withdrawal.getCurrency().getIso(), estimatedFeeRate);

        // >>>>> 6. we verify next if duskswap has enough balance on its own
        if(Double.parseDouble(overallBalance.get().getWithdrawalBalance()) <= Double.parseDouble(withdrawal.getAmount()) +
                                                                              Double.parseDouble(withdrawal.getDuskFeesCrypto()) +
                                                                              estimatedFeesGivenByAPI
        ) {
            log.error("[" + new Date() + "] => DUSKSWAP INSUFFICIENT BALANCE >>>>>>>> confirm :: WithdrawalController.java");
            return new ResponseEntity<>(CodeErrors.INSUFFICIENT_BALANCE_DUSKSWAP, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>> 7. and then check if the user's has enough amount of crypto in his account
        Double estimatedTotalUserExpense = Double.parseDouble(dto.getAmount()) +
                                           estimatedFeesGivenByAPI +
                                           Double.parseDouble(withdrawal.getDuskFeesCrypto());
        if(!accountService.isBalanceSufficient(account, dto.getCurrencyId(), Double.toString(estimatedTotalUserExpense))) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> confirm :: WithdrawalController.java");
            return new ResponseEntity<>(CodeErrors.INSUFFICIENT_BALANCE_USER, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        log.info("[" + new Date() + "] => USER(" + user.get().getEmail()+ ") estimated fee rate = " + estimatedFeeRate + ", estimated fees = " + estimatedFeesGivenByAPI + "" +
                ", estimated user expenses = " + estimatedTotalUserExpense);

        // ============================== Performing the send transaction =================================
        // >>>>> 8. Afterwards, we send properly crypto to user
        WalletTransaction walletTransaction = new WalletTransaction();
        walletTransaction.setFeeRate(estimatedFeeRate);
        walletTransaction.setNoChange(false);
        walletTransaction.setRbf(null);
        walletTransaction.setSelectedInputs(null);
        walletTransaction.setProceedWithBroadcast(true);
        walletTransaction.setProceedWithPayjoin(false);
        WalletTransactionDestination destination = new WalletTransactionDestination();
        destination.setDestination(dto.getToAddress());
        destination.setAmount(dto.getAmount());
        destination.setSubtractFromAmount(false); // we take fees outside the amount we want to send to user
        List<WalletTransactionDestination> destinations = new ArrayList<>();
        destinations.add(destination);
        walletTransaction.setDestinations(destinations);

        TransactionBlock block = invoiceService.sendCrypto(walletTransaction, withdrawal.getCurrency().getIso());
        if(block == null) {
            log.error("[" + new Date() + "] => ERROR: CAN'T MAKE SEND CRYPTO >>>>>>>> confirm :: WithdrawalController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        log.info("[" + new Date() + "] => USER(" + user.get().getEmail()+ ") TRANSACTION BLOCK =======> " + block);

        // ============================== Saving of the withdrawal =====================================
        // >>>>> 9. we get the actual network fees used in send transaction
        Double actualNetworkFees = Math.abs(
                Math.abs(Double.parseDouble(block.getAmount())) - Math.abs(Double.parseDouble(dto.getAmount()))
        );
        Double totalAmountToDebit = Double.parseDouble(withdrawal.getAmount()) + actualNetworkFees + Double.parseDouble(withdrawal.getDuskFeesCrypto());

        // >>>>> 10. after that, we save the withdrawal with all the fees
        withdrawal.setNetworkFees(Double.toString(actualNetworkFees));
        withdrawal.setTransactionHash(block.getTransactionHash());
        Withdrawal savedWithdrawal = withdrawalService.saveWithdrawal(withdrawal);

        log.info("[" + new Date() + "] => USER(" + user.get().getEmail()+ ") actual network fees = " + actualNetworkFees + ", total amount to debit = " + totalAmountToDebit);
        // ================================= Updating the balances =====================================
        // >>>>> 11. we first debit the user's account
        accountService.debitAccount(withdrawal.getExchangeAccount(), withdrawal.getCurrency(), Double.toString(totalAmountToDebit));

        // >>>>> 12. after that we decrease the overall withdrawal balance and add the dusk fees into the total earnings section
        Double newDuskswapWithdrawalBalance = Double.parseDouble(overallBalance.get().getWithdrawalBalance()) - totalAmountToDebit;
        Double newDuskswapTotalEarnings = Double.parseDouble(overallBalance.get().getTotalEarnings()) + Double.parseDouble(withdrawal.getDuskFeesCrypto());
        overallBalance.get().setWithdrawalBalance(Double.toString(newDuskswapWithdrawalBalance));
        overallBalance.get().setTotalEarnings(Double.toString(newDuskswapTotalEarnings));
        overallBalanceService.saveBalance(overallBalance.get());

        // >>>>> 13. next we update the verification code in order the user won't send the same request twice
        // (this is to avoid issues like debiting multiple time an account for a single operation)
        verificationCodeService.updateCode(user.get().getEmail(), DefaultProperties.VERIFICATION_WITHDRAWAL_SELL_PURPOSE);

        return ResponseEntity.ok(true);

    }


}
