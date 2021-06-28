package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.models.Invoice;
import com.dusk.duskswap.commons.models.Currency;
import org.springframework.http.ResponseEntity;

public interface InvoiceService {

    ResponseEntity<Invoice> createInvoice(Invoice invoice);
    Invoice getInvoice(String invoiceId);
    Currency getInvoiceCurrency(Invoice invoice);

}
