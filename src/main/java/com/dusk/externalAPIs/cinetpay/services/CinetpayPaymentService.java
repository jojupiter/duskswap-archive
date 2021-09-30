package com.dusk.externalAPIs.cinetpay.services;

import com.dusk.externalAPIs.cinetpay.models.PaymentInit;
import com.dusk.externalAPIs.cinetpay.models.PaymentInitResponse;
import com.dusk.externalAPIs.cinetpay.models.VerificationResponse;

public interface CinetpayPaymentService {

    PaymentInitResponse initializePayment(PaymentInit init);
    VerificationResponse checkPayment(String apiKey, String siteId, String paymentToken);

}
