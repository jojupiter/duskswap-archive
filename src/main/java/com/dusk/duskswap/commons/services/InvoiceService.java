package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.models.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface InvoiceService {

    ResponseEntity<Invoice> createInvoice(Invoice invoice);
    Invoice getInvoice(String invoiceId);
    Currency getInvoiceCurrency(Invoice invoice);
    List<InvoicePayment> getPaymentMethods(String invoiceId, Boolean onlyAccountedPayments);

    WalletBalance getCryptoBalance(String cryptoCode);
    String sendCrypto(WalletTransaction walletTransaction, String cryptoCode); // return transaction in hex (tx)

}
