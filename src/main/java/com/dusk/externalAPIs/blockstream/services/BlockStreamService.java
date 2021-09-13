package com.dusk.externalAPIs.blockstream.services;

import com.dusk.externalAPIs.apiInterfaces.models.TransactionInfos;
import com.dusk.externalAPIs.blockstream.models.Transaction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BlockStreamService {

    private static final String domainUrl = "https://blockstream.info/api";
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // to avoid error when we have an unknown property
    }

    public static Transaction getTransaction(String txId) {
        // input checking
        if(txId == null || (txId != null && txId.isEmpty())) {
            log.error("[" + new Date() + "] => txId null >>>>>>>> getTransaction :: BlockStreamService.java");
            return null;
        }

        URL url = null;
        try {
            // >>>>> 1. we create the request
            url = new URL(domainUrl + "/tx/" + txId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.connect();

            // >>>>> 2. we check if the response code is valid (it's valid status code = 200)
            if(conn.getResponseCode() != 200) {
                log.error("[" + new Date() + "] => CONN STATUS != 200 >>>>>>>> getTransaction :: BlockStreamService.java");
                return null;
            }

            // >>>>> 3. finally we read the response and return it as a Transaction object
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
                Transaction transaction = mapper.readValue(content.toString(), Transaction.class);
                return transaction;
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



    public static Map<String, String> getFeesEstimation() {

        URL url = null;
        try {
            // >>>>> 1. we create the request
            url = new URL(domainUrl + "/fee-estimates");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.connect();

            // >>>>> 2. we check if the response code is valid (it's valid status code = 200)
            if(conn.getResponseCode() != 200) {
                log.error("[" + new Date() + "] => CONN STATUS != 200 >>>>>>>> getFeesEstimation :: BlockStreamService.java");
                return null;
            }

            // >>>>> 3. finally we read the response and return it as a HashMap of key and value strings
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
                Map<String, String> fees = mapper.readValue(content.toString(), new TypeReference<HashMap<String, String>>() {});
                return fees;
            }

        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static Long getBlocksHeight() {

        URL url = null;
        try {
            // >>>>> 1. we create the request
            url = new URL(domainUrl + "/blocks/tip/height");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.connect();

            // >>>>> 2. we check if the response code is valid (it's valid status code = 200)
            if(conn.getResponseCode() != 200) {
                log.error("[" + new Date() + "] => CONN STATUS != 200 >>>>>>>> getFeesEstimation :: BlockStreamService.java");
                return null;
            }

            // >>>>> 3. finally we read the response and return it as a HashMap of key and value strings
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
                Long blockHeight = mapper.readValue(content.toString(), Long.class);
                return blockHeight;
            }

        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

}
