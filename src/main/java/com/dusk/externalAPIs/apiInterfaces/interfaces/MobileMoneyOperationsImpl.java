package com.dusk.externalAPIs.apiInterfaces.interfaces;

import com.dusk.externalAPIs.apiInterfaces.models.MobileMoneyPaymentRequest;
import com.dusk.externalAPIs.apiInterfaces.models.MobileMoneyPaymentResponse;
import com.dusk.externalAPIs.cinetpay.constants.CinetpayParams;
import com.dusk.externalAPIs.cinetpay.models.PaymentInit;
import com.dusk.externalAPIs.cinetpay.models.PaymentInitResponse;
import com.dusk.externalAPIs.cinetpay.models.VerificationResponse;
import com.dusk.externalAPIs.cinetpay.services.CinetpayPaymentService;
import com.dusk.externalAPIs.cinetpay.services.CinetpayTransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class MobileMoneyOperationsImpl implements MobileMoneyOperations {

    @Autowired
    private CinetpayTransferService cinetpayTransferService;
    @Autowired
    private CinetpayPaymentService cinetpayPaymentService;

    @Override
    public MobileMoneyPaymentResponse performPayment(MobileMoneyPaymentRequest request) {
        // input checking
        if(request == null) {
            log.error("[" + new Date() + "] => INPUT NULL (PAYMENT REQUEST) >>>>>>>> performPayment :: MobileMoneyOperationsImpl.java");
            return null;
        }

        // >>>>> 1. we initialize the payment
        PaymentInit init = new PaymentInit();
        init.setAmount(request.getAmount());
        init.setApikey(CinetpayParams.API_KEY);
        init.setSite_id(CinetpayParams.SITE_ID);
        init.setChannels(request.getChannels());
        init.setCurrency(request.getCurrencyIso());
        init.setCustomer_id(request.getCustomerId());
        init.setCustomer_surname(request.getCustomerFirstName());
        init.setCustomer_name(request.getCustomerLastName());
        init.setDescription(request.getDescription());
        init.setLang(request.getLang());
        init.setTransaction_id(request.getTransactionId());
        init.setMetadata(request.getMetadata());
        init.setReturn_url(CinetpayParams.RETURN_URL);
        // TODO: set the notification URL

        PaymentInitResponse initResponse = cinetpayPaymentService.initializePayment(init);
        if(initResponse == null) {
            log.error("[" + new Date() + "] => CANNOT PERFORM PAYMENT INITIALIZATION >>>>>>>> performPayment :: MobileMoneyOperationsImpl.java");
            return null;
        }

        // >>>>> 2. return the payment info
        if(!initResponse.getCode().equals(CinetpayParams.STATUS_CREATED)) {
            log.error("[" + new Date() + "] => CINETPAY ERROR, statusCode = " + initResponse.getCode() + ">>>>>>>> performPayment :: MobileMoneyOperationsImpl.java");
            return null;
        }

        MobileMoneyPaymentResponse response = new MobileMoneyPaymentResponse();
        response.setDescription(initResponse.getDescription());
        response.setMessage(initResponse.getMessage());
        response.setPaymentToken(initResponse.getData().getPayment_token());
        response.setPaymentUrl(initResponse.getData().getPayment_url());
        response.setStatus(initResponse.getCode());

        return response;
    }

    @Override
    public VerificationResponse checkPaymentStatus(String token) {
        if (token == null || (token != null && token.isEmpty())) {
            log.error("[" + new Date() + "] => INPUT NULL (PAYMENT REQUEST) >>>>>>>> checkPaymentStatus :: MobileMoneyOperationsImpl.java");
            return null;
        }

        VerificationResponse response = cinetpayPaymentService.checkPayment(CinetpayParams.API_KEY, CinetpayParams.SITE_ID, token);
        if(response == null) {
            log.error("[" + new Date() + "] => RESPONSE NULL >>>>>>>> checkPaymentStatus :: MobileMoneyOperationsImpl.java");
            return null;
        }

        return response;
    }

}
