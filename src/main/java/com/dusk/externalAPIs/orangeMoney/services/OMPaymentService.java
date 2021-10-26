package com.dusk.externalAPIs.orangeMoney.services;

import com.dusk.externalAPIs.orangeMoney.models.AccessToken;
import com.dusk.externalAPIs.orangeMoney.models.PaymentRequest;
import com.dusk.externalAPIs.orangeMoney.models.PaymentResponse;

import java.util.Map;

public interface OMPaymentService {

    AccessToken getAccessToken(String clientId, String clientSecret, String customerKey);
    PaymentResponse initPayment(PaymentRequest request, String accessToken);
    Map<String, String> checkPayment(Map<String, Object> paymentData, String accessToken);

}
