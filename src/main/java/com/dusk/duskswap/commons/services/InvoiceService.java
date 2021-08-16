package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.models.Invoice;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.InvoicePayment;
import com.dusk.duskswap.commons.models.WalletTransaction;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface InvoiceService {

    ResponseEntity<Invoice> createInvoice(Invoice invoice);
    Invoice getInvoice(String invoiceId);
    Currency getInvoiceCurrency(Invoice invoice);
    List<InvoicePayment> getPaymentMethods(String invoiceId, Boolean onlyAccountedPayments);

    String sendCrypto(WalletTransaction walletTransaction, String cryptoCode); // return transaction in hex (tx)

}
