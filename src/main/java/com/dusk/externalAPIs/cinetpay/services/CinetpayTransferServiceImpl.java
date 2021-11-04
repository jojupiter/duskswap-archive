package com.dusk.externalAPIs.cinetpay.services;

import com.dusk.externalAPIs.cinetpay.constants.CinetpayParams;
import com.dusk.externalAPIs.cinetpay.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CinetpayTransferServiceImpl implements CinetpayTransferService {

    private ObjectMapper mapper = new ObjectMapper();
    private OkHttpClient client;
    private HttpClient httpClient;
    private static final String AUTHENTICATION_URL = "https://client.cinetpay.com/v1/auth/login";
    private static final String TRANSFER_ACCOUNT_BALANCE_URL = "https://client.cinetpay.com/v1/transfer/check/balance";
    private static final String ADDING_TRANSFER_CONTACT_URL = "https://client.cinetpay.com/v1/transfer/contact";
    private static final String SENDING_URL = "https://client.cinetpay.com/v1/transfer/money/send/contact";
    private static final String TRANSFER_INFO_URL = "https://client.cinetpay.com/v1/transfer/check/money";

    public CinetpayTransferServiceImpl() {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // to avoid error when we have an unknown property
        client = new OkHttpClient().newBuilder().build();
        httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public String getAuthToken(String lang, String apikey, String password) {
        if(
                lang == null || (lang != null && lang.isEmpty()) ||
                apikey == null || (apikey != null && apikey.isEmpty()) ||
                password == null || (password != null && password.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT OR NULL >>>>>>>> getAuthToken :: CinetpayTransferServiceImpl.java" +
                    " ===== lang = " + lang + ", apikey = " + apikey + ", password = " + password);
            return null;
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("apikey", apikey);
        parameters.put("password", password);

        String form = parameters.keySet().stream()
                .map(key -> key + "=" + URLEncoder.encode(parameters.get(key), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTHENTICATION_URL + "?lang=" + lang))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {

            HttpResponse<?> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            AuthSuccess authSuccess = null;
            log.info("AUTHENTICATION RESPONSE BODY >>>>>> " + response.body().toString());
            if(response.body() != null || (response.body() != null && response.body().toString().isEmpty())) {
                Map<String, Object> mapResponse = mapper.readValue(response.body().toString(), new TypeReference<Map<String, Object>>(){});

                if( Integer.parseInt(mapResponse.get("code").toString()) == 0 ) {
                    authSuccess = mapper.readValue(response.body().toString(), AuthSuccess.class);
                    return authSuccess.getData().get("token");
                }

            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    public TransferBalance getTransferBalance(String token, String lang) {
        if(
                token == null || (token != null && token.isEmpty()) ||
                lang == null || (lang != null && lang.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT OR NULL >>>>>>>> getTransferBalance :: CinetpayTransferServiceImpl.java" +
                    " ===== lang = " + lang + ", token = " + token);
            return null;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TRANSFER_ACCOUNT_BALANCE_URL + "?token=" + token + "&lang=" + lang))
                .build();

        try {
            HttpResponse<?> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.body() != null || (response.body() != null && response.body().toString().isEmpty())) {
                Map<String, Object> mapResponse = mapper.readValue(response.body().toString(), new TypeReference<Map<String, Object>>(){});

                TransferBalance balance = null;
                if( Integer.parseInt(mapResponse.get("code").toString()) == 0 ) {
                    balance = mapper.readValue(response.body().toString(), TransferBalance.class);
                    return balance;
                }
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Contact transferMoney(String token, String lang, List<Contact> contacts) {
        if(
                token == null || (token != null && token.isEmpty()) ||
                lang == null || (lang != null && lang.isEmpty()) ||
                contacts == null || (contacts != null && contacts.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT OR NULL >>>>>>>> transferMoney :: CinetpayTransferServiceImpl.java" +
                    " ===== lang = " + lang + ", token = " + token + ", contacts = " + contacts);
            return null;
        }


        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SENDING_URL + "?token=" + token + "&lang=" + lang))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("data=" + mapper.writeValueAsString(contacts)))
                    .build();

            HttpResponse<?> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("RESPONSE BODY >>>>>> " + response.body() + "  >>>>>>>> transferMoney :: CinetpayTransferServiceImpl.java");
            Map<String, Object> responseJson = null;

            if(response.body() != null || (response.body() != null && response.body().toString().isEmpty())) {
                responseJson = mapper.readValue(response.body().toString(), new TypeReference<Map<String, Object>>() {});
                if(Integer.parseInt(responseJson.get("code").toString()) == CinetpayParams.STATUS_TRANSFER_SUCCESS) {
                    return ( (List<List<Contact>>) responseJson.get("data") ).get(0).get(0);
                }
                return null;
            }

        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Contact> addContacts(String token, String lang, List<Contact> contacts) {
        if(
                token == null || (token != null && token.isEmpty()) ||
                lang == null || (lang != null && lang.isEmpty()) ||
                contacts == null || (contacts != null && contacts.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT OR NULL >>>>>>>> addContacts :: CinetpayTransferServiceImpl.java" +
                    " ===== lang = " + lang + ", token = " + token + ", contacts = " + contacts);
            return null;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ADDING_TRANSFER_CONTACT_URL + "?token=" + token + "&lang=" + lang))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("data:" + mapper.writeValueAsString(contacts)))
                    .build();

            HttpResponse<?> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> responseJson = null;

            log.info("ADD CONTACTS RESPONSE BODY >>>>>>> " + response.body());

            if(response.body() != null || (response.body() != null && response.body().toString().isEmpty())) {
                responseJson = mapper.readValue(response.body().toString(), new TypeReference<Map<String, Object>>() {});
                if(Integer.parseInt(responseJson.get("code").toString()) == CinetpayParams.STATUS_TRANSFER_SUCCESS) {
                    return ((List<List<Contact>>)responseJson.get("data")).get(0);
                }
            }

        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public TransferInfo getTransferInfo(String token, String clientTransactionId, String lang) {
        if(
                token == null || (token != null && token.isEmpty()) ||
                clientTransactionId == null || (clientTransactionId != null && clientTransactionId.isEmpty()) ||
                lang == null || (lang != null && lang.isEmpty())
        ) {
            log.error("[" + new Date() + "] INPUT NULL OR EMPTY >>>>>>>> getTransferInfo :: CinetpayTransferServiceImpl.java" +
                    " ====== token = " + token + ", clientTxId = " + clientTransactionId + ", lang = " + lang);
            return null;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TRANSFER_INFO_URL + "?token=" + token + "&lang=" + lang + "&client_transaction_id=" + clientTransactionId))
                .build();

        try {
            HttpResponse<?> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.body() != null || (response.body() != null && response.body().toString().isEmpty())) {
                return mapper.readValue(response.body().toString(), TransferInfo.class);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

}
