package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.models.Invoice;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Optional;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private String btcpayServerApi = "71108d2c1722443951e445849678ed01591c64f1";
    private String domainUrl = "https://09btcpay.kifipay.com/api/v1/stores/";
    private String storeAddress = "CML9V2zd6KDrkFMPLc6yTdZSznhV7GYxGEBT98ShwYer";
    private ObjectMapper mapper = new ObjectMapper();
    private Logger logger = LoggerFactory.getLogger(InvoiceServiceImpl.class);

    @Autowired
    private CurrencyRepository currencyRepository;

    @Override
    public ResponseEntity<Invoice> createInvoice(Invoice invoice) {
        if(invoice == null)
            return ResponseEntity.badRequest().body(null);

        try {
            // setting up connection to btcpay and post the invoice
            URL url = new URL(domainUrl + storeAddress + "/invoices");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Authorization", "token " + btcpayServerApi);
            //conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "Basic RUVHZHZvMEwwTzdvcm9XVnBkN29pN2tkN3lvYmhsV0dJTERyb2Q3MFowZA==");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.connect();

            String invoiceAsString = mapper.writeValueAsString(invoice);
            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = invoiceAsString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // reading response
            // first check if the response code is valid, if not we send back an unprocessable entity status code
            if(conn.getResponseCode() != 200)
                return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);

            // if status response == ok, then we read the response
            StringBuilder response = new StringBuilder();

            try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // if response is not null, we return the source code of the checkout link
            if(response.toString() != null && !response.toString().isEmpty()) {
                Invoice invoiceCreated = mapper.readValue(response.toString(), Invoice.class);
                return ResponseEntity.ok(invoiceCreated);
            }

        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        catch (ProtocolException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return null;
    }

    @Override
    public Invoice getInvoice(String invoiceId) {

        // setting up connection to btcpay and post the invoice
        URL url = null;
        try {
            url = new URL(domainUrl + storeAddress + "/invoices/" + invoiceId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Authorization", "token " + btcpayServerApi);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.connect();

            // reading response
            // first check if the response code is valid, if not we send back an unprocessable entity status code
            if(conn.getResponseCode() != 200)
                return null;

            // if response is not null, we return the source code of the checkout link
            StringBuilder content;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                content = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }

            if((content.toString() != null && content.toString().isEmpty()) || content.toString() == null) {
                Invoice invoice = mapper.readValue(content.toString(), Invoice.class);
                return invoice;
            }

        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (ProtocolException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public Currency getInvoiceCurrency(Invoice invoice) {
        if(invoice == null)
            return null;

        Optional<Currency> currency = currencyRepository.findByIso(invoice.getCurrency());

        return currency.get();
    }
}
