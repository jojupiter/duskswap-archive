package com.dusk.duskswap.deposit.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.Invoice;
import com.dusk.duskswap.commons.models.WebhookEvent;
import com.dusk.duskswap.commons.services.InvoiceService;
import com.dusk.duskswap.commons.services.UtilitiesService;
import com.dusk.duskswap.deposit.entityDto.DepositDto;
import com.dusk.duskswap.deposit.entityDto.DepositPage;
import com.dusk.duskswap.deposit.entityDto.DepositResponseDto;
import com.dusk.duskswap.deposit.models.Deposit;
import com.dusk.duskswap.deposit.services.DepositService;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.deposit.services.DepositServiceImpl;
import com.dusk.duskswap.usersManagement.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/deposits")
public class DepositController {

    @Autowired
    private DepositService depositService;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UtilitiesService utilitiesService;
    private Logger logger = LoggerFactory.getLogger(DepositController.class);

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/user-all", produces = "application/json")
    public ResponseEntity<DepositPage> getAllUserDeposits(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserDeposits :: DepositController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
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
    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<DepositResponseDto> createDeposit(@RequestBody DepositDto dto) {
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> createDeposit :: DepositController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return depositService.createCryptoDeposit(user.get(), dto);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping(value = "/update-address", produces = "application/json")
    public ResponseEntity<Boolean> updateDepositDestinationAddress(@RequestParam(name = "depositId") Long depositId,
                                                                   @RequestParam(name = "toAddress") String toAddress) {
        return depositService.updateDestinationAddress(depositId, toAddress);
    }

    @PutMapping(value = "/update-status", produces = "application/json")
    public void updateDepositStatus(@RequestBody WebhookEvent webhookEvent) {
        // first we check the input and the invoice id
        if(
                 webhookEvent == null ||
                (webhookEvent != null && (webhookEvent.getInvoiceId().equals("") || webhookEvent.getInvoiceId() == null))
        )
            return;

        // Then we verify the status of the corresponding invoice
        Invoice invoice = invoiceService.getInvoice(webhookEvent.getInvoiceId());
        if(invoice == null)
            return;

        // We update the deposit status if it has changed
        Deposit deposit = depositService.getDepositByInvoiceId(webhookEvent.getInvoiceId());
        if(deposit != null && deposit.getStatus().getName().equals(DefaultProperties.STATUS_TRANSACTION_CRYPTO_RADICAL + invoice.getStatus())) {
            depositService.updateDepositStatus(deposit.getId(), DefaultProperties.STATUS_TRANSACTION_CRYPTO_RADICAL + invoice.getStatus());
        }
        if(deposit == null)
            return;

        // if the status is "Complete", then we update the account balance
        if(invoice.getStatus().equals("Complete")) {
            ExchangeAccount account = accountService.getAccountById(deposit.getId());
            if(account == null)
                return;

            Currency currency = invoiceService.getInvoiceCurrency(invoice);
            if(currency == null)
                return;

            accountService.fundAccount(account, currency, invoice.getAmount());
        }

    }

}
