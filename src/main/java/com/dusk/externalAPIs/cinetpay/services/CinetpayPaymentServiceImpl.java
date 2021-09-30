package com.dusk.externalAPIs.cinetpay.services;

import com.dusk.externalAPIs.cinetpay.models.PaymentInit;
import com.dusk.externalAPIs.cinetpay.models.PaymentInitResponse;
import com.dusk.externalAPIs.cinetpay.models.VerificationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Slf4j
public class CinetpayPaymentServiceImpl implements CinetpayPaymentService {

    private ObjectMapper mapper = new ObjectMapper();
    private static final String PAYMENT_INITIALIZATION_URL = "https://api-checkout.cinetpay.com/v2/payment";
    private static final String PAYMENT_STATUS_CHECKING_URL = "https://api-checkout.cinetpay.com/v2/payment/check";

    public CinetpayPaymentServiceImpl() {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // to avoid error when we have an unknown property
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

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        try {
            RequestBody body = RequestBody.create(mapper.writeValueAsString(init).getBytes(StandardCharsets.UTF_8));
            Request request = new Request.Builder()
                    .url(PAYMENT_INITIALIZATION_URL)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();;

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            log.info("RESPONSE BODY >>>>>> " + responseBody + "  >>>>>>>> initializePayment :: CinetpayPaymentServiceImpl.java");

            PaymentInitResponse initResponse = null;
            if(responseBody != null || (responseBody != null && responseBody.isEmpty())) {
                initResponse = mapper.readValue(responseBody, PaymentInitResponse.class);
                return initResponse;
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

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("apikey", apiKey)
                    .add("site_id", siteId)
                    .add("token", paymentToken)
                    .build();

            Request request = new Request.Builder()
                    .url(PAYMENT_STATUS_CHECKING_URL)
                    .post(formBody)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            log.info("RESPONSE BODY >>>>>> " + responseBody + "  >>>>>>>> checkPayment :: CinetpayPaymentServiceImpl.java");

            VerificationResponse verificationResponse = null;
            if(responseBody != null || (responseBody != null && responseBody.isEmpty())) {
                verificationResponse = mapper.readValue(responseBody, VerificationResponse.class);
                return verificationResponse;
            }

        }
        catch (JsonProcessingException e) {
            log.error("[" + new Date() + "] => " + e.getMessage() + ">>>>>>>> checkPayment :: CinetpayPaymentServiceImpl.java");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

}
