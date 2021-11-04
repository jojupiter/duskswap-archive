package com.dusk.externalAPIs.cinetpay.services;

import com.dusk.externalAPIs.cinetpay.models.PaymentInit;
import com.dusk.externalAPIs.cinetpay.models.PaymentInitResponse;
import com.dusk.externalAPIs.cinetpay.models.VerificationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CinetpayPaymentServiceImpl implements CinetpayPaymentService {

    private ObjectMapper mapper = new ObjectMapper();
    private HttpClient httpClient;
    private static final String PAYMENT_INITIALIZATION_URL = "https://api-checkout.cinetpay.com/v2/payment";
    private static final String PAYMENT_STATUS_CHECKING_URL = "https://api-checkout.cinetpay.com/v2/payment/check";

    public CinetpayPaymentServiceImpl() {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // to avoid error when we have an unknown property
        httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public PaymentInitResponse initializePayment(PaymentInit init) {

        if(
                init == null ||
                (
                        init != null &&
                                (
                                        init.getAmount() == null || (init.getAmount() != null && init.getAmount().isEmpty()) ||
                                        init.getTransaction_id() == null || (init.getTransaction_id() != null && init.getAmount().isEmpty()) ||
                                        init.getCurrency() == null || (init.getCurrency() != null && init.getCurrency().isEmpty()) ||
                                        init.getApikey() == null || (init.getApikey() != null && init.getApikey().isEmpty()) ||
                                        init.getSite_id() == null || (init.getSite_id() != null && init.getSite_id().isEmpty())
                                )
                )
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT OR NULL (PAYMENT INIT) >>>>>>>> initializePayment :: CinetpayPaymentServiceImpl.java" +
                    " init = " + init);
            return null;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PAYMENT_INITIALIZATION_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(init)))
                    .build();

            HttpResponse<?> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            PaymentInitResponse initResponse = null;
            if(response.body() != null || (response.body() != null && response.body().toString().isEmpty())) {
                initResponse = mapper.readValue(response.body().toString(), PaymentInitResponse.class);
                return initResponse;
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
    public VerificationResponse checkPayment(String apiKey, String siteId, String paymentToken) {
        if(
                apiKey == null || (apiKey != null && apiKey.isEmpty()) ||
                siteId == null || (siteId != null && siteId.isEmpty()) ||
                paymentToken == null || (paymentToken != null && paymentToken.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT OR NULL (PAYMENT INIT) >>>>>>>> initializePayment :: CinetpayPaymentServiceImpl.java" +
                    " ==== apiKey = " + apiKey + ", siteId = " + siteId + ", txIdOrPaymentToken = " + paymentToken);
            return null;
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("apikey", apiKey);
        parameters.put("site_id", siteId);
        parameters.put("token", paymentToken);

        String form = parameters.keySet().stream()
                .map(key -> key + "=" + URLEncoder.encode(parameters.get(key), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PAYMENT_STATUS_CHECKING_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form/*requestBody.toString()*/))
                .build();

        try {
            HttpResponse<?> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            VerificationResponse verificationResponse = null;
            if(response.body() != null || (response.body() != null && response.body().toString().isEmpty())) {
                verificationResponse = mapper.readValue(response.body().toString(), VerificationResponse.class);
                return verificationResponse;
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
