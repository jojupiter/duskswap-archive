package com.dusk.duskswap.deposit.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.commons.models.Invoice;
import com.dusk.duskswap.commons.models.WebhookEvent;
import com.dusk.duskswap.commons.services.InvoiceService;
import com.dusk.duskswap.deposit.entityDto.DepositDto;
import com.dusk.duskswap.deposit.models.Deposit;
import com.dusk.duskswap.deposit.services.DepositService;
import com.dusk.duskswap.commons.models.Currency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/all", produces = "application/json", params = {"token"})
    public ResponseEntity<List<Deposit>> getAllUserDeposits(@RequestParam(name = "token") String token) {
        return depositService.getAllUserDeposits(token);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<List<Deposit>> getAllDeposits() {
        return depositService.getAllUserDeposits();
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<String> createDeposit(@RequestBody DepositDto dto) {
        return depositService.createCryptoDeposit(dto);
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
        if(deposit != null && deposit.getStatus().getName().equals("TRANSACTION_CRYPTO_" + invoice.getStatus())) {
            depositService.updateDepositStatus(deposit.getId(), "TRANSACTION_CRYPTO_" + invoice.getStatus());
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
