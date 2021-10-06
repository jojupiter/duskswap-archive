package com.dusk.externalAPIs.cinetpay.services;

import com.dusk.duskswap.commons.models.InvoicePayment;
import com.dusk.externalAPIs.cinetpay.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class CinetpayTransferServiceImpl implements CinetpayTransferService {

    private ObjectMapper mapper = new ObjectMapper();
    private OkHttpClient client;
    private static final String AUTHENTICATION_URL = "https://client.cinetpay.com/v1/auth/login";
    private static final String TRANSFER_ACCOUNT_BALANCE_URL = "https://client.cinetpay.com/v1/transfer/check/balance";
    private static final String ADDING_TRANSFER_CONTACT_URL = "https://client.cinetpay.com/v1/transfer/contact";
    private static final String SENDING_URL = "https://client.cinetpay.com/v1/transfer/money/send/contact";

    public CinetpayTransferServiceImpl() {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // to avoid error when we have an unknown property
        client = new OkHttpClient().newBuilder().build();
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

        HttpUrl.Builder urlBuilder = HttpUrl.parse(AUTHENTICATION_URL).newBuilder();
        urlBuilder.addQueryParameter("lang", apikey);
        String url = urlBuilder.build().toString();

        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("apikey", apikey)
                    .add("password", password)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            log.info("RESPONSE BODY >>>>>> " + responseBody + "  >>>>>>>> getAuthToken :: CinetpayTransferServiceImpl.java");

            AuthSuccess authSuccess = null;
            if(responseBody != null || (responseBody != null && responseBody.isEmpty())) {
                authSuccess = mapper.readValue(responseBody, AuthSuccess.class);
                return authSuccess.getData().get("token");
            }

        }
        catch (JsonProcessingException e) {
            log.error("[" + new Date() + "] => " + e.getMessage() + ">>>>>>>> initializePayment :: CinetpayTransferServiceImpl.java");
        }
        catch (IOException e) {
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

        HttpUrl.Builder urlBuilder = HttpUrl.parse(TRANSFER_ACCOUNT_BALANCE_URL).newBuilder();
        urlBuilder.addQueryParameter("token", token);
        urlBuilder.addQueryParameter("lang", lang);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .method("GET", null).build();
        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            log.info("RESPONSE BODY >>>>>> " + responseBody + "  >>>>>>>> getTransferBalances :: CinetpayTransferServiceImpl.java");

            TransferBalance balance = null;
            if(responseBody != null || (responseBody != null && responseBody.isEmpty())) {
                balance = mapper.readValue(responseBody, TransferBalance.class);
                return balance;
            }
        }
        catch (JsonProcessingException e) {
            log.error("[" + new Date() + "] => " + e.getMessage() + ">>>>>>>> getTransferBalance :: CinetpayTransferServiceImpl.java");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public TransferInfo transferMoney(String token, String lang, List<Contact> contacts) {
        if(
                token == null || (token != null && token.isEmpty()) ||
                lang == null || (lang != null && lang.isEmpty()) ||
                contacts == null || (contacts != null && contacts.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT OR NULL >>>>>>>> transferMoney :: CinetpayTransferServiceImpl.java" +
                    " ===== lang = " + lang + ", token = " + token + ", contacts = " + contacts);
            return null;
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse(SENDING_URL).newBuilder();
        urlBuilder.addQueryParameter("token", token);
        urlBuilder.addQueryParameter("lang", lang);
        String url = urlBuilder.build().toString();

        try {
            RequestBody body = RequestBody.create(mapper.writeValueAsString(contacts).getBytes(StandardCharsets.UTF_8));

            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body).build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            log.info("RESPONSE BODY >>>>>> " + responseBody + "  >>>>>>>> transferMoney :: CinetpayTransferServiceImpl.java");

            TransferInfo info = null;
            if(responseBody != null || (responseBody != null && responseBody.isEmpty())) {
                info = mapper.readValue(responseBody, TransferInfo.class);
                return info;
            }
        }
        catch (JsonProcessingException e) {
            log.error("[" + new Date() + "] => " + e.getMessage() + ">>>>>>>> transferMoney :: CinetpayTransferServiceImpl.java");
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

        HttpUrl.Builder urlBuilder = HttpUrl.parse(ADDING_TRANSFER_CONTACT_URL).newBuilder();
        urlBuilder.addQueryParameter("token", token);
        urlBuilder.addQueryParameter("lang", lang);
        String url = urlBuilder.build().toString();

        try {
            RequestBody body = RequestBody.create(mapper.writeValueAsString(contacts).getBytes(StandardCharsets.UTF_8));

            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body).build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            log.info("RESPONSE BODY >>>>>> " + responseBody + "  >>>>>>>> addContacts :: CinetpayTransferServiceImpl.java");

            List<Contact> contactList = null;
            if(responseBody != null || (responseBody != null && responseBody.isEmpty())) {
                contactList = mapper.readValue(responseBody, new TypeReference<List<Contact>>(){});
                return contactList;
            }
        }
        catch (JsonProcessingException e) {
            log.error("[" + new Date() + "] => " + e.getMessage() + "   >>>>>>>> addContacts :: CinetpayTransferServiceImpl.java");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
