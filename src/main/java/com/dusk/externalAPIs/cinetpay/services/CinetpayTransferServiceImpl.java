package com.dusk.externalAPIs.cinetpay.services;

import com.dusk.externalAPIs.cinetpay.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Slf4j
public class CinetpayTransferServiceImpl implements CinetpayTransferService {

    private ObjectMapper mapper = new ObjectMapper();
    private static final String AUTHENTICATION_URL = "https://client.cinetpay.com/v1/auth/login";
    private static final String TRANSFER_ACCOUNT_BALANCE_URL = "https://client.cinetpay.com/v1/transfer/check/balance";
    private static final String ADDING_TRANSFER_CONTACT_URL = "https://client.cinetpay.com/v1/transfer/contact";
    private static final String SENDING_URL = "https://client.cinetpay.com/v1/transfer/money/send/contact";


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

        OkHttpClient client = new OkHttpClient().newBuilder().build();
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
            log.error("[" + new Date() + "] => " + e.getMessage() + ">>>>>>>> initializePayment :: CinetpayPaymentServiceImpl.java");
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

        
        return null;
    }

    @Override
    public TransferInfo transferMoney(String token, String lang, Contact contact) {
        if(
                token == null || (token != null && token.isEmpty()) ||
                lang == null || (lang != null && lang.isEmpty()) ||
                contact == null ||
                (
                    contact != null &&
                            (
                                contact.getPhone() == null || (contact.getPhone() != null && contact.getPhone().isEmpty()) ||
                                contact.getAmount() == null
                            )
                )
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT OR NULL >>>>>>>> transferMoney :: CinetpayTransferServiceImpl.java" +
                    " ===== lang = " + lang + ", token = " + token + ", contact = " + contact);
            return null;
        }

        return null;
    }

}
