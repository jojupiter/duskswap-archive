package com.dusk.duskswap.deposit.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.commons.miscellaneous.CodeErrors;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.miscellaneous.Utilities;
import com.dusk.duskswap.commons.models.*;
import com.dusk.duskswap.commons.services.InvoiceService;
import com.dusk.duskswap.commons.services.UtilitiesService;
import com.dusk.duskswap.deposit.entityDto.DepositDto;
import com.dusk.duskswap.deposit.entityDto.DepositHashCount;
import com.dusk.duskswap.deposit.entityDto.DepositHashPage;
import com.dusk.duskswap.deposit.entityDto.DepositPage;
import com.dusk.duskswap.deposit.models.Deposit;
import com.dusk.duskswap.deposit.models.DepositHash;
import com.dusk.duskswap.deposit.services.DepositService;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.externalAPIs.apiInterfaces.interfaces.BlockExplorerOperations;
import com.dusk.externalAPIs.apiInterfaces.models.TransactionInfos;
import com.dusk.externalAPIs.blockstream.models.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.dusk.duskswap.commons.miscellaneous.Utilities.findPayment;

@RestController
@CrossOrigin("*")
@RequestMapping("/deposits")
@Slf4j
public class DepositController {

    @Autowired
    private DepositService depositService;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UtilitiesService utilitiesService;
    @Autowired
    private BlockExplorerOperations blockExplorerOperations;

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/user-all", produces = "application/json")
    public ResponseEntity<?> getAllUserDeposits(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserDeposits :: DepositController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return depositService.getAllUserDeposits(user.get(), currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<DepositPage> getAllDeposits(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return depositService.getAllDeposits(currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/deposit-hash/user-all", produces = "application/json")
    public ResponseEntity<?> getAllUserDepositHashes(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserDepositHashes :: DepositController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        ExchangeAccount account = accountService.getAccountByUser(user.get());
        if(account == null) {
            log.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getAllUserDepositHashes :: DepositController.java");
            return new ResponseEntity<>(CodeErrors.EXCHANGE_ACCOUNT_NOT_EXIST, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return depositService.getAllUserDepositHashes(account, currentPage, pageSize);

    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/create")
    @Transactional
    public ResponseEntity<?> createDeposit(@RequestBody DepositDto dto) throws Exception {
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> createDeposit :: DepositController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return depositService.createCryptoDeposit(user.get(), dto);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping(value = "/update-address", produces = "application/json")
    public ResponseEntity<Boolean> updateDepositDestinationAddress(@RequestParam(name = "depositId") Long depositId,
                                                                   @RequestParam(name = "toAddress") String toAddress) {
        return depositService.updateDestinationAddress(depositId, toAddress);
    }

    @PostMapping(value = "/received-payment", produces = "application/json") // here the webhook's call back
    @Transactional
    public void receivePayment(@RequestHeader("BTCPay-Sig") String btcpaySig,
                               @RequestBody WebhookEvent webhookEvent) throws Exception {

        // header checking
        //TODO: check the hashmac 256 of the header

        // >>>>> 1. we find the deposit with the corresponding invoiceId and the total deposit hash count associated with that invoice
        DepositHashCount hashCount = depositService.countDepositHashes(webhookEvent.getInvoiceId());

        if(hashCount == null) {
            log.error("[" + new Date() + "] => DEPOSIT HASH COUNT NOT PRESENT >>>>>>>> receivePayment :: DepositController.java");
            return;
        }
        // >>>>> 2. we get the invoice payments associated to this invoice
        List<InvoicePayment> invoicePayments = invoiceService.getPaymentMethods(webhookEvent.getInvoiceId(), true);
        if(invoicePayments == null || (invoicePayments != null && invoicePayments.isEmpty())) {
            log.error("[" + new Date() + "] => INVOICE PAYMENTS UNAVAILABLE >>>>>>>> receivePayment :: DepositController.java");
            return;
        }

        // >>>>> 3. we then check the total number of deposit hashes. If it's greater than the max + 1 authorized number we don't create another deposit hash
        if(hashCount.getTotalHashCount() > DefaultProperties.MAX_NUMBER_OF_TRANSACTION_FOR_INVOICE + 1) {
            log.info("[" + new Date() + "] => CAN'T MAKE ANOTHER DEPOSIT FOR THIS INVOICE (NUMBER OF AUTHOIZED DEPOSITS IS MAX ) >>>>>>>> receivePayment :: DepositController.java");
            return;
        }
        // >>>>> 4. we next create the deposit hash
        if(
                hashCount.getTotalHashCount() >= 0 &&
                hashCount.getTotalHashCount() <= DefaultProperties.MAX_NUMBER_OF_TRANSACTION_FOR_INVOICE + 1
        )
        depositService.createDepositHash(invoicePayments, hashCount.getDeposit());

    }

    @PostMapping(value = "/check-hash-status", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Transactional
    public ResponseEntity<?> checkDepositHashStatus(@RequestParam(name = "depositId") Long depositId,
                                                    @RequestParam(name = "tx") String transactionHash) throws Exception {
        // input checking
        if(
                depositId == null ||
                transactionHash == null || (transactionHash != null && transactionHash.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> checkDepositHashStatus :: DepositController.java" +
                    " ======= depositId = " + depositId + ", tx = " + transactionHash);
            return ResponseEntity.badRequest().body(CodeErrors.INPUT_ERROR_CODE);
        }

        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> createDeposit :: DepositController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 1. we get the deposit associated with depositId
        Optional<Deposit> deposit = depositService.getDepositById(depositId);
        if(!deposit.isPresent()) {
            log.error("[" + new Date() + "] => DEPOSIT NOT PRESENT >>>>>>>> checkDepositHashStatus :: DepositController.java");
            return new ResponseEntity<>(CodeErrors.DEPOSIT_NOT_EXISTING, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>> 2. we get the depositHash associated with transactionHash
        Optional<DepositHash> depositHash = depositService.getDepositHashByTransaction(transactionHash);
        if(!depositHash.isPresent()) {
            log.error("[" + new Date() + "] => DEPOSIT HASH NOT PRESENT >>>>>>>> checkDepositHashStatus :: DepositController.java");
            return new ResponseEntity<>(CodeErrors.DEPOSIT_HASH_NOT_EXISTING, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. if depositHash exists, the we can check the status of the payment
        List<InvoicePayment> invoicePayments = invoiceService.getPaymentMethods(deposit.get().getInvoiceId(), true);
        if(invoicePayments == null || (invoicePayments != null && invoicePayments.isEmpty())) {
            log.error("[" + new Date() + "] => INVOICE PAYMENTS UNAVAILABLE >>>>>>>> checkDepositHashStatus :: DepositController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // here we get the payment
        Payment correspondingPayment = Utilities.findPayment(invoicePayments, transactionHash);
        if(correspondingPayment == null) {
            log.error("[" + new Date() + "] => CAN'T FIND PAYMENT >>>>>>>> checkDepositHashStatus :: DepositController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // we check the transaction information
        /*TransactionInfos transactionInfos = blockExplorerOperations.getTransaction(correspondingPayment.getId(), deposit.get().getCurrency().getIso());*/

        String paymentStatus = DefaultProperties.STATUS_TRANSACTION_CRYPTO_RADICAL + correspondingPayment.getStatus();
        //we only update status whenever deposit hash status != btcpay payment status
        if(!correspondingPayment.getStatus().equals(paymentStatus)) {
            DepositHash updatedDepositHash = depositService.updateDepositHashStatus(depositHash.get(), paymentStatus);
            if(updatedDepositHash == null) {
                log.error("[" + new Date() + "] => DIDN'T UPDATE DEPOSIT HASH STATUS >>>>>>>> checkDepositHashStatus :: DepositController.java");
                return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        // >>>>> 4. If the status is "Settled", then we fund the user's account
         if(correspondingPayment.getStatus().equals(DefaultProperties.BTCPAY_INVOICE_STATUS_SETTLED)) {
            // first we get the exchange account
            ExchangeAccount account = accountService.getAccountByUser(user.get());
            if(account == null) {
                log.error("[" + new Date() + "] => CAN'T FIND USER'S EXCHANGE ACCOUNT >>>>>>>> checkDepositHashStatus :: DepositController.java");
                return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
            }
            accountService.fundAccount(account, deposit.get().getCurrency(), depositHash.get().getAmount());
            return ResponseEntity.ok(true);
        }

        return ResponseEntity.ok(false);
    }

}
