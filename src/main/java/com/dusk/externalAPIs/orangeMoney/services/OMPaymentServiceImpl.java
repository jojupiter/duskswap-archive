package com.dusk.externalAPIs.orangeMoney.services;

import com.dusk.externalAPIs.cinetpay.models.PaymentInitResponse;
import com.dusk.externalAPIs.cinetpay.models.VerificationResponse;
import com.dusk.externalAPIs.orangeMoney.constants.OMConstants;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private HttpClient httpClient;

    public OMPaymentServiceImpl() {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // to avoid error when we have an unknown property
        httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public AccessToken getAccessToken() {
        // input checking
        if(OMConstants.AUTHORIZATION_HEADER == null ||
                (OMConstants.AUTHORIZATION_HEADER != null && OMConstants.AUTHORIZATION_HEADER.isEmpty())) {
            log.error("[" + new Date() + "] => AUTHORIZATION TOKEN INCORRECT OR NULL >>>>>>>> initializePayment :: CinetpayPaymentServiceImpl.java");
            return null;
        }

        try {
            String bodyString = "grand_type=client_credentials";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PAYMENT_INITIALIZATION_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Basic " + OMConstants.AUTHORIZATION_HEADER)
                    .POST(HttpRequest.BodyPublishers.ofString(bodyString))
                    .build();

            HttpResponse<?> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            AccessToken accessToken = null;

            if(response.body() != null || (response.body() != null && response.body().toString().isEmpty())) {
                accessToken = mapper.readValue(response.body().toString(), AccessToken.class);
                return accessToken;
            }

        }
        catch (JsonProcessingException e) {
            log.error("[" + new Date() + "] => " + e.getMessage() + ">>>>>>>> getAccessToken :: OMPaymentServiceImpl.java");
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
    public PaymentResponse initPayment(PaymentRequest paymentRequest, String accessToken) {
        if(
                paymentRequest == null ||
                accessToken == null || (accessToken != null && accessToken.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT ERROR >>>>>>>> initPayment :: OMPaymentServiceImpl.java" +
                    " ======= request = " + paymentRequest + ", accessToken = " + accessToken);
            return null;
        }

        try {
            paymentRequest.setMerchant_key(OMConstants.MERCHANT_KEY);
            String paymentRequestString = mapper.writeValueAsString(paymentRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PAYMENT_INITIALIZATION_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(paymentRequestString))
                    .build();

            HttpResponse<?> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            PaymentResponse paymentResponse = null;

            if(response.body() != null || (response.body() != null && response.body().toString().isEmpty())) {
                paymentResponse = mapper.readValue(response.body().toString(), PaymentResponse.class);
                return paymentResponse;
            }

        }
        catch (JsonProcessingException e) {
            log.error("[" + new Date() + "] => " + e.getMessage() + ">>>>>>>> initPayment :: OMPaymentServiceImpl.java");
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
            String paymentDataString = mapper.writeValueAsString(paymentData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PAYMENT_INITIALIZATION_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(paymentDataString))
                    .build();

            HttpResponse<?> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, String> paymentStatus = null;

            if(response.body() != null || (response.body() != null && response.body().toString().isEmpty())) {
                paymentStatus = mapper.readValue(response.body().toString(), new TypeReference<HashMap<String, String>>() {});
                return paymentStatus;
            }

        }
        catch (JsonProcessingException e) {
            log.error("[" + new Date() + "] => " + e.getMessage() + ">>>>>>>> initPayment :: OMPaymentServiceImpl.java");
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
