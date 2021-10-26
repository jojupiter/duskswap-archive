package com.dusk.externalAPIs.orangeMoney.services;

import com.dusk.externalAPIs.cinetpay.models.VerificationResponse;
import com.dusk.externalAPIs.orangeMoney.models.AccessToken;
import com.dusk.externalAPIs.orangeMoney.models.PaymentRequest;
import com.dusk.externalAPIs.orangeMoney.models.PaymentResponse;
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
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class OMPaymentServiceImpl implements OMPaymentService {

    private ObjectMapper mapper = new ObjectMapper();
    private static String ACCESS_TOKEN_URL = "https://api.orange.com/oauth/v2/token";
    private static String PAYMENT_INITIALIZATION_URL = "https://api.orange.com/orange-money-webpay/dev/v1/webpayment";
    private static String PAYMENT_NOTIFICATION_URL = "https://api.orange.com/orange-money-webpay/dev/v1/transactionstatus";
    private OkHttpClient client;

    public OMPaymentServiceImpl() {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // to avoid error when we have an unknown property
        client = new OkHttpClient().newBuilder().build();
    }

    @Override
    public AccessToken getAccessToken(String clientId, String clientSecret, String customerKey) {
        // TODO: input checking
        try {
            String bodyString = "grand_type=" + clientId + "_" + clientSecret;
            RequestBody body = RequestBody.create(mapper.writeValueAsString(bodyString).getBytes(StandardCharsets.UTF_8));

            Request request = new Request.Builder()
                    .url(ACCESS_TOKEN_URL)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Basic " + customerKey)
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            AccessToken accessToken = null;
            if(responseBody != null || (responseBody != null && responseBody.isEmpty())) {
                accessToken = mapper.readValue(responseBody, AccessToken.class);
                return accessToken;
            }

        }
        catch (JsonProcessingException e) {
            log.error("[" + new Date() + "] => " + e.getMessage() + ">>>>>>>> getAccessToken :: OMPaymentServiceImpl.java");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public PaymentResponse initPayment(PaymentRequest request, String accessToken) {
        if(
                request == null ||
                accessToken == null || (accessToken != null && accessToken.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT ERROR >>>>>>>> initPayment :: OMPaymentServiceImpl.java" +
                    " ======= request = " + request + ", accessToken = " + accessToken);
            return null;
        }

        try {
            RequestBody body = RequestBody.create(mapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8));

            Request req = new Request.Builder()
                    .url(ACCESS_TOKEN_URL)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            Response response = client.newCall(req).execute();
            String responseBody = response.body().string();

            PaymentResponse paymentResponse = null;
            if(responseBody != null || (responseBody != null && responseBody.isEmpty())) {
                paymentResponse = mapper.readValue(responseBody, PaymentResponse.class);
                return paymentResponse;
            }

        }
        catch (JsonProcessingException e) {
            log.error("[" + new Date() + "] => " + e.getMessage() + ">>>>>>>> initPayment :: OMPaymentServiceImpl.java");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map<String, String> checkPayment(Map<String, Object> paymentData, String accessToken) {
        if(
            paymentData == null ||
            accessToken == null || (accessToken != null && accessToken.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT ERROR >>>>>>>> initPayment :: OMPaymentServiceImpl.java" +
                    " ======= paymentData = " + paymentData + ", accessToken = " + accessToken);
            return null;
        }

        try {
            RequestBody body = RequestBody.create(mapper.writeValueAsString(paymentData).getBytes(StandardCharsets.UTF_8));

            Request req = new Request.Builder()
                    .url(ACCESS_TOKEN_URL)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            Response response = client.newCall(req).execute();
            String responseBody = response.body().string();

            Map<String, String> paymentStatus = null;
            if(responseBody != null || (responseBody != null && responseBody.isEmpty())) {
                paymentStatus = mapper.readValue(responseBody, new TypeReference<HashMap<String, String>>() {});
                return paymentStatus;
            }

        }
        catch (JsonProcessingException e) {
            log.error("[" + new Date() + "] => " + e.getMessage() + ">>>>>>>> initPayment :: OMPaymentServiceImpl.java");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
