package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.models.Invoice;

public interface InvoiceService {

    Invoice createInvoice(Invoice invoice);
    String checkInvoiceStatus(Long invoiceId);

}
