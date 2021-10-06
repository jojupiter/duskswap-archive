package com.dusk.externalAPIs.apiInterfaces.interfaces;

import com.dusk.externalAPIs.apiInterfaces.models.MobileMoneyPaymentRequest;
import com.dusk.externalAPIs.apiInterfaces.models.MobileMoneyPaymentResponse;
import com.dusk.externalAPIs.cinetpay.models.VerificationResponse;

public interface MobileMoneyOperations {

    MobileMoneyPaymentResponse performPayment(MobileMoneyPaymentRequest request);
    VerificationResponse checkPaymentStatus(String token);

}
