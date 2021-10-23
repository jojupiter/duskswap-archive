package com.dusk.externalAPIs.apiInterfaces.interfaces;

import com.dusk.externalAPIs.apiInterfaces.models.*;
import com.dusk.externalAPIs.cinetpay.models.VerificationResponse;

public interface MobileMoneyOperations {

    // payment
    MobileMoneyPaymentResponse performPayment(MobileMoneyPaymentRequest request, String paymentAPIIso);
    VerificationResponse checkPaymentStatus(String token);

    // transfer
    AuthResponse authenticate(AuthRequest request);
    Double getTransferBalance(String token, String lang);
    MobileMoneyTransferResponse performTransfer(String token, String lang, MobileMoneyTransferInfo info);
    MobileMoneyTransferInfo getTransferInformation(String token, String transactionId, String lang);

}
