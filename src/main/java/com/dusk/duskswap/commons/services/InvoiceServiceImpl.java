package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.application.AppProperties;
import com.dusk.duskswap.commons.models.*;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private CurrencyRepository currencyRepository;

    @Override
    public ResponseEntity<Invoice> createInvoice(Invoice invoice) {
        if(invoice == null) {
            log.error("[" + new Date() + "] => INPUT NULL >>>>>>>> createInvoice :: InvoiceServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // setting up connection to btcpay and post the invoice
            URL url = new URL(AppProperties.BTCPAY_SERVER_DOMAIN_URL + AppProperties.BTCPAY_RECEIVE_STORE_ID + "/invoices");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Authorization", "token " + AppProperties.BTCPAY_RECEIVE_API);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.connect();
            // here we write the request's body
            String invoiceAsString = mapper.writeValueAsString(invoice);
            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = invoiceAsString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // reading response
            // first check if the response code is valid, if not we send back an unprocessable entity status code
            if(conn.getResponseCode() != 200) {
                log.error("[" + new Date() + "] => CONN STATUS != 200 >>>>>>>> createInvoice :: InvoiceServiceImpl.java");
                return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
            }

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
            log.error("[" + new Date() + "] => MalformedURLException :: message = "+ e.getMessage() +">>>>>>>> createInvoice :: InvoiceServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        catch (ProtocolException e) {
            log.error("[" + new Date() + "] => ProtocolException :: message = "+ e.getMessage() +">>>>>>>> createInvoice :: InvoiceServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        catch (IOException e) {
            log.error("[" + new Date() + "] => IOException :: message = "+ e.getMessage() +">>>>>>>> createInvoice :: InvoiceServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return null;
    }

    @Override
    public Invoice getInvoice(String invoiceId) {

        if(invoiceId == null || (invoiceId != null && invoiceId.isEmpty())) {
            log.error("[" + new Date() + "] => INPUT NULL (InvoiceId) >>>>>>>> getInvoice :: InvoiceServiceImpl.java");
            return null;
        }
        // setting up connection to btcpay and post the invoice
        URL url = null;
        try {
            url = new URL(AppProperties.BTCPAY_SERVER_DOMAIN_URL + AppProperties.BTCPAY_RECEIVE_STORE_ID + "/invoices/" + invoiceId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Authorization", "token " + AppProperties.BTCPAY_RECEIVE_API);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.connect();

            // reading response
            // first check if the response code is valid
            if(conn.getResponseCode() != 200) {
                log.error("[" + new Date() + "] => CONN STATUS != 200 >>>>>>>> getInvoice :: InvoiceServiceImpl.java");
                return null;
            }

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

            if(content.toString() != null && !content.toString().isEmpty()) {
                Invoice invoice = mapper.readValue(content.toString(), Invoice.class);
                return invoice;
            }

        }
        catch (MalformedURLException e) {
            log.error("[" + new Date() + "] => MalformedURLException :: message = "+ e.getMessage() +">>>>>>>> getInvoice :: InvoiceServiceImpl.java");
        }
        catch (ProtocolException e) {
            log.error("[" + new Date() + "] => ProtocolException :: message = "+ e.getMessage() +">>>>>>>> getInvoice :: InvoiceServiceImpl.java");
        }
        catch (IOException e) {
            log.error("[" + new Date() + "] => IOException :: message = "+ e.getMessage() +">>>>>>>> getInvoice :: InvoiceServiceImpl.java");
        }

        return null;
    }


    @Override
    public Currency getInvoiceCurrency(Invoice invoice) {
        if(invoice == null) {
            log.error("[" + new Date() + "] => INPUT NULL (Invoice) >>>>>>>> getInvoiceCurrency :: InvoiceServiceImpl.java");
            return null;
        }

        Optional<Currency> currency = currencyRepository.findByIso(invoice.getCurrency());

        return currency.get();
    }

    @Override
    public List<InvoicePayment> getPaymentMethods(String invoiceId, Boolean onlyAccountedPayments) {
        // input checking
        if(invoiceId == null || (invoiceId != null && invoiceId.isEmpty())) {
            log.error("[" + new Date() + "] => INPUT NULL (INVOICE ID) >>>>>>>> getPaymentMethods :: InvoiceServiceImpl.java");
            return null;
        }

        URL url = null;
        try {
            url = new URL(AppProperties.BTCPAY_SERVER_DOMAIN_URL + AppProperties.BTCPAY_RECEIVE_STORE_ID + "/invoices/" + invoiceId + "/payment-methods");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Authorization", "token " + AppProperties.BTCPAY_RECEIVE_API);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.connect();

            // reading response
            // first check if the response code is valid
            if(conn.getResponseCode() != 200) {
                log.error("[" + new Date() + "] => CONN STATUS != 200 >>>>>>>> getPaymentMethods :: InvoiceServiceImpl.java");
                return null;
            }

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

            if(content.toString() != null && !content.toString().isEmpty()) {
                List<InvoicePayment> invoicePayment = mapper.readValue(content.toString(), new TypeReference<List<InvoicePayment>>(){});
                return invoicePayment;
            }

        }
        catch (MalformedURLException e) {
            log.error("[" + new Date() + "] => MalformedURLException :: message = "+ e.getMessage() +">>>>>>>> getPaymentMethods :: InvoiceServiceImpl.java");
        }
        catch (IOException e) {
            log.error("[" + new Date() + "] => IOException :: message = "+ e.getMessage() +">>>>>>>> getPaymentMethods :: InvoiceServiceImpl.java");
        }

        return null;
    }

    @Override
    public TransactionBlock sendCrypto(WalletTransaction walletTransaction, String cryptoCode) {
        // input checking
        if(walletTransaction == null) {
            log.error("[" + new Date() + "] => INPUT NULL >>>>>>>> sendCrypto :: InvoiceServiceImpl.java");
            return null;
        }

        URL url = null;
        try {
            url = new URL(AppProperties.BTCPAY_SERVER_DOMAIN_URL + AppProperties.BTCPAY_SEND_STORE_ID + "/payment-methods/OnChain/" + cryptoCode + "/wallet/transactions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Authorization", "token " + AppProperties.BTCPAY_SEND_API);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.connect();

            // here we write the request's body
            String walletTransactionString = mapper.writeValueAsString(walletTransaction);
            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = walletTransactionString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // reading response
            // first check if the response code is valid
            if(conn.getResponseCode() != 200) {
                log.error("[" + new Date() + "] => CONN STATUS != 200 >>>>>>>> sendCrypto :: InvoiceServiceImpl.java");
                return null;
            }

            // if response's status = 200, we read the response
            StringBuilder response = new StringBuilder();

            try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            if(response.toString() != null && !response.toString().isEmpty()) {
                TransactionBlock block = mapper.readValue(response.toString(), TransactionBlock.class);
                return block;
            }

        }
        catch (MalformedURLException e) {
            log.error("[" + new Date() + "] => MalformedURLException :: message = "+ e.getMessage() +">>>>>>>> sendCrypto :: InvoiceServiceImpl.java");
        }
        catch (ProtocolException e) {
            log.error("[" + new Date() + "] => ProtocolException :: message = "+ e.getMessage() +">>>>>>>> sendCrypto :: InvoiceServiceImpl.java");
        }
        catch (IOException e) {
            log.error("[" + new Date() + "] => IOException :: message = "+ e.getMessage() +">>>>>>>> sendCrypto :: InvoiceServiceImpl.java");
        }

        return null;
    }


    @Override
    public WalletBalance getCryptoBalance(String cryptoCode) {
        // input checking
        if(cryptoCode == null || (cryptoCode != null && cryptoCode.isEmpty())) {
            log.error("[" + new Date() + "] => INPUT NULL >>>>>>>> getCryptoBalance :: InvoiceServiceImpl.java");
            return null;
        }

        URL url = null;
        try {
            url = new URL(AppProperties.BTCPAY_SERVER_DOMAIN_URL + AppProperties.BTCPAY_SEND_STORE_ID + "/payment-methods/OnChain/" + cryptoCode + "/wallet");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Authorization", "token " + AppProperties.BTCPAY_SEND_API);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.connect();

            // reading response
            // first check if the response code is valid
            if(conn.getResponseCode() != 200) {
                log.error("[" + new Date() + "] => CONN STATUS != 200 >>>>>>>> getCryptoBalance :: InvoiceServiceImpl.java");
                return null;
            }

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

            if(content.toString() != null && !content.toString().isEmpty()) {
                WalletBalance walletBalance = mapper.readValue(content.toString(), WalletBalance.class);
                return walletBalance;
            }

        }
        catch (MalformedURLException e) {
            log.error("[" + new Date() + "] => MalformedURLException :: message = "+ e.getMessage() +">>>>>>>> getCryptoBalance :: InvoiceServiceImpl.java");
        }
        catch (ProtocolException e) {
            log.error("[" + new Date() + "] => ProtocolException :: message = "+ e.getMessage() +">>>>>>>> getCryptoBalance :: InvoiceServiceImpl.java");
        } catch (IOException e) {
            log.error("[" + new Date() + "] => IOException :: message = "+ e.getMessage() +">>>>>>>> getCryptoBalance :: InvoiceServiceImpl.java");
        }

        return null;
    }

}
