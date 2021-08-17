package com.dusk.duskswap.deposit.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.commons.miscellaneous.CodeErrors;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.Invoice;
import com.dusk.duskswap.commons.models.InvoicePayment;
import com.dusk.duskswap.commons.models.WebhookEvent;
import com.dusk.duskswap.commons.services.InvoiceService;
import com.dusk.duskswap.commons.services.UtilitiesService;
import com.dusk.duskswap.deposit.entityDto.DepositDto;
import com.dusk.duskswap.deposit.entityDto.DepositPage;
import com.dusk.duskswap.deposit.models.Deposit;
import com.dusk.duskswap.deposit.services.DepositService;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.usersManagement.models.User;
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

    @PostMapping(value = "/update-status", produces = "application/json")
    @Transactional
    public void updateDepositStatus(@RequestBody WebhookEvent webhookEvent) throws Exception {
        // >>>>> 1. first we check the input and the invoice id
        if(
                 webhookEvent == null ||
                (webhookEvent != null && (webhookEvent.getInvoiceId().equals("") || webhookEvent.getInvoiceId() == null))
        )
            return;

        // >>>>> 2. Then we verify the status of the corresponding invoice
        Invoice invoice = invoiceService.getInvoice(webhookEvent.getInvoiceId());
        if(invoice == null) {
            log.error("[" + new Date() + "] => CAN'T FIND INVOICE with id = " + webhookEvent.getInvoiceId() + " >>>>>>>> updateDepositStatus :: DepositController.java");
            return;
        }

        // >>>>> 3. We check the amount paid (even if that amount is less than the expected, we consider the deposit completed)
        List<InvoicePayment> invoicePayments = invoiceService.getPaymentMethods(invoice.getId(), true);
        if(invoicePayments == null || (invoicePayments != null && invoicePayments.isEmpty())) {
            log.error("[" + new Date() + "] => PAYMENT LIST EMPTY OR NULL >>>>>>>> updateDepositStatus :: DepositController.java");
            return;
        }

        Double amountPaid = 0.0;
        for(InvoicePayment invoicePayment: invoicePayments) {
            if(invoicePayment.getTotalPaid() != null && !invoicePayment.getTotalPaid().isEmpty())
                amountPaid += Double.parseDouble(invoicePayment.getTotalPaid());
        }

        // >>>>> 4. We update the deposit status if it has changed
        Optional<Deposit> deposit = depositService.getDepositByInvoiceId(webhookEvent.getInvoiceId());
        if(deposit.isPresent()) {
            // if status == complete, we save it as "Settled" in deposit. ("Settled" for us means that the deposit is done)
            String newDepositStatus = DefaultProperties.STATUS_TRANSACTION_CRYPTO_RADICAL + invoice.getStatus();

            // if the payment is either partial or over or even settled, we set the deposit status as "Settled"
            if(
                    invoice.getStatus().equals(DefaultProperties.BTCPAY_INVOICE_STATUS_SETTLED) ||
                    invoice.getStatus().equals(DefaultProperties.BTCPAY_INVOICE_STATUS_COMPLETE) ||
                    invoice.getAdditionalStatus().equals(DefaultProperties.BTCPAY_INVOICE_STATUS_PAID_LATE) ||
                    invoice.getAdditionalStatus().equals(DefaultProperties.BTCPAY_INVOICE_STATUS_PAID_OVER) ||
                    invoice.getAdditionalStatus().equals(DefaultProperties.BTCPAY_INVOICE_STATUS_PAID_PARTIAL)
            )
                newDepositStatus = DefaultProperties.STATUS_TRANSACTION_CRYPTO_SETTLED;

            depositService.updateDepositStatus(
                    deposit.get(),
                    newDepositStatus,
                    Double.toString(amountPaid)
            );
        }
        if(!deposit.isPresent()) {
            log.error("[" + new Date() + "] => CAN'T FIND DEPOSIT WITH INVOICE ID = " + webhookEvent.getInvoiceId() + " >>>>>>>> updateDepositStatus :: DepositController.java");
            return;
        }

        // >>>>> 5. if the status is "Settled" or the additional status is paid over, late or partial, then we update the account balance
        if(
                invoice.getStatus().equals(DefaultProperties.BTCPAY_INVOICE_STATUS_SETTLED) ||
                invoice.getStatus().equals(DefaultProperties.BTCPAY_INVOICE_STATUS_COMPLETE) ||
                invoice.getAdditionalStatus().equals(DefaultProperties.BTCPAY_INVOICE_STATUS_PAID_LATE) ||
                invoice.getAdditionalStatus().equals(DefaultProperties.BTCPAY_INVOICE_STATUS_PAID_OVER) ||
                invoice.getAdditionalStatus().equals(DefaultProperties.BTCPAY_INVOICE_STATUS_PAID_PARTIAL)
        ) {
            ExchangeAccount account = accountService.getAccountById(deposit.get().getExchangeAccount().getId());
            if(account == null) {
                log.error("[" + new Date() + "] => CAN'T FIND USER'S EXCHANGE ACCOUNT >>>>>>>> updateDepositStatus :: DepositController.java");
                return;
            }

            Currency currency = invoiceService.getInvoiceCurrency(invoice);
            if(currency == null) {
                log.error("[" + new Date() + "] => CAN'T FIND CORRESPONDING CURRENCY >>>>>>>> updateDepositStatus :: DepositController.java");
                return;
            }

            accountService.fundAccount(account, currency, invoice.getAmount());
        }

    }

}
