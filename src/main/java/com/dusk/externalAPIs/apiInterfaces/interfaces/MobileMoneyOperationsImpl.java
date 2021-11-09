package com.dusk.externalAPIs.apiInterfaces.interfaces;

import com.dusk.externalAPIs.apiInterfaces.models.*;
import com.dusk.externalAPIs.cinetpay.constants.CinetpayParams;
import com.dusk.externalAPIs.cinetpay.models.*;
import com.dusk.externalAPIs.cinetpay.services.CinetpayPaymentService;
import com.dusk.externalAPIs.cinetpay.services.CinetpayTransferService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class MobileMoneyOperationsImpl implements MobileMoneyOperations {

    @Autowired
    private CinetpayTransferService cinetpayTransferService;
    @Autowired
    private CinetpayPaymentService cinetpayPaymentService;
    private final String DEFAULT_LANGUAGE = "fr";

    // ========================= payment =============================

    @Override
    public MobileMoneyPaymentResponse performPayment(MobileMoneyPaymentRequest request, String paymentAPIIso) {
        // input checking
        if(
                request == null ||
                paymentAPIIso == null || (paymentAPIIso != null && paymentAPIIso.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT NULL (PAYMENT REQUEST) >>>>>>>> performPayment :: MobileMoneyOperationsImpl.java" +
                    " ====== request = " + request + ", paymentAPI = " + paymentAPIIso);
            return null;
        }

        if(paymentAPIIso.equals("CINET") || paymentAPIIso.equals("CINETPAY") || paymentAPIIso.startsWith("CINE")) {
            // >>>>> 1. we initialize the payment
            request.setApiUsed(CinetpayParams.API_NAME); // this is to make difference between apis that we will use for payments.
            // this will helps us to differentiate the api fees (e.g: API_NAME = CINETPAY => api_fees = 3.5%, API_NAME = OM => api_fees = ......)
            PaymentInit init = new PaymentInit();
            init.setAmount(request.getAmount());
            init.setApikey(CinetpayParams.API_KEY);
            init.setSite_id(CinetpayParams.SITE_ID);
            init.setChannels(request.getChannels() == null || (request.getChannels() != null && request.getChannels().isEmpty()) ?
                    "MOBILE_MONEY" :
                    request.getChannels());
            init.setCurrency(request.getCurrencyIso() == null || (request.getCurrencyIso() != null && request.getCurrencyIso().isEmpty()) ?
                    "XAF" :
                    request.getCurrencyIso());
            init.setCustomer_id(request.getCustomerId());
            init.setCustomer_surname(request.getCustomerFirstName());
            init.setCustomer_name(request.getCustomerLastName());
            init.setDescription(request.getDescription());
            init.setLang(request.getLang() == null || (request.getLang() != null && request.getLang().isEmpty()) ?
                    DEFAULT_LANGUAGE :
                    request.getLang());
            init.setTransaction_id(request.getTransactionId());
            init.setMetadata(request.getMetadata());
            init.setReturn_url(CinetpayParams.RETURN_URL);
            init.setNotify_url(CinetpayParams.PAYMENT_NOTIFICATION_URL);
            init.setDescription(CinetpayParams.DEFAULT_PAYMENT_DESCRIPTION);

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
            response.setApiFees(CinetpayParams.CINETPAY_PAYMENT_FEES_CM);
            response.setDescription(initResponse.getDescription());
            response.setMessage(initResponse.getMessage());
            response.setPaymentToken(initResponse.getData().getPayment_token());
            response.setPaymentUrl(initResponse.getData().getPayment_url());
            response.setStatus(initResponse.getCode());

            return response;
        }

        return null;
    }

    @Override
    public VerificationResponse
    checkPaymentStatus(String token, String siteId) {
        if (token == null || (token != null && token.isEmpty())) {
            log.error("[" + new Date() + "] => INPUT NULL (PAYMENT REQUEST) >>>>>>>> checkPaymentStatus :: MobileMoneyOperationsImpl.java");
            return null;
        }

        VerificationResponse response = cinetpayPaymentService.checkPayment(CinetpayParams.API_KEY, siteId, token);
        if(response == null) {
            log.error("[" + new Date() + "] => RESPONSE NULL >>>>>>>> checkPaymentStatus :: MobileMoneyOperationsImpl.java");
            return null;
        }

        return response;
    }

    // ============================= Transfer ======================================
    @Override
    public AuthResponse authenticate(AuthRequest request) {
        // input checking
        if(request == null) {
            request = new AuthRequest();
            request.setLang("fr");
        }

        String token = cinetpayTransferService.getAuthToken(
                request.getLang() == null || (request.getLang() != null && request.getLang().isEmpty()) ? DEFAULT_LANGUAGE : request.getLang(),
                CinetpayParams.API_KEY,
                CinetpayParams.TRANSFER_PASSWORD
        );

        if(token == null || (token != null && token.isEmpty())) {
            log.error("[" + new Date() + "] => CANNOT GET THE TOKEN >>>>>>>> authenticate :: MobileMoneyOperationsImpl.java");
            return null;
        }

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);

        return authResponse;
    }

    @Override
    public Double getTransferBalance(String token, String lang) {
        if(token == null || (token != null && token.isEmpty())) {
            log.error("[" + new Date() + "] => TOKEN NULL >>>>>>>> authenticate :: MobileMoneyOperationsImpl.java");
            return null;
        }

        TransferBalance balance = cinetpayTransferService.getTransferBalance(token,
                lang == null || (lang != null && lang.isEmpty()) ? DEFAULT_LANGUAGE : lang);

        if(balance == null) {
            log.error("[" + new Date() + "] => BALANCE NULL >>>>>>>> authenticate :: MobileMoneyOperationsImpl.java");
            return null;
        }

        return balance.getData().getAvailable();
    }

    @Override
    public MobileMoneyTransferResponse performTransfer(String token, String lang, MobileMoneyTransferInfo info) {
        if(
                token == null || (token != null && token.isEmpty()) ||
                info == null ||
                (
                    info != null && (
                                info.getPhone() == null || (info.getPhone() != null && info.getPhone().isEmpty()) ||
                                info.getAmount() == null ||
                                info.getEmail() == null || (info.getEmail() != null && info.getEmail().isEmpty())
                            )
                )
        ) {
            log.error("[" + new Date() + "] => BALANCE NULL >>>>>>>> performTransfer :: MobileMoneyOperationsImpl.java");
            return null;
        }
        if(lang == null || (lang != null && !lang.isEmpty()))
            lang = DEFAULT_LANGUAGE;

        // ================================== ADDING CONTACT ===============================================
        Contact contact = new Contact();
        contact.setPrefix(info.getPhonePrefix() != null && !info.getPhonePrefix().isEmpty() ?
                info.getPhonePrefix() :
                "237"
        );
        contact.setPhone(info.getPhone());
        contact.setEmail(info.getEmail());
        contact.setName(info.getLastName());
        contact.setSurname(info.getFirstName());

        List<Contact> contactList = new ArrayList<>();
        contactList.add(contact);

        List<Contact> savedContacts = cinetpayTransferService.addContacts(token,
                lang,
                contactList
        );

        // =============================== EXECUTING TRANSFER ============================================
        contactList.get(0).setAmount(Double.parseDouble(info.getAmount()));
        contactList.get(0).setNotify_url(CinetpayParams.TRANSFER_NOTIFICATION_URL);
        contactList.get(0).setClient_transaction_id(info.getTransactionId());

        Contact transfer = cinetpayTransferService.transferMoney(
                token,
                lang,
                contactList
        );

        if(transfer == null) {
            log.error("[" + new Date() + "] => TRANSFER CAN'T BE EXECUTED >>>>>>>> performTransfer :: MobileMoneyOperationsImpl.java");
            return null;
        }

        MobileMoneyTransferResponse response = new MobileMoneyTransferResponse();
        response.setAmount(Double.toString(transfer.getAmount()));
        response.setClientTransactionId(transfer.getClient_transaction_id());
        response.setStatus(transfer.getStatus());
        response.setReceiverPhone(transfer.getPhone());

        return response;
    }

    @Override
    public MobileMoneyTransferInfo getTransferInformation(String token, String transactionId, String lang) {
        if(
                token == null || (token != null && token.isEmpty()) ||
                transactionId == null || (transactionId != null && transactionId.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> getTransferInformation :: MobileMoneyOperationsImpl.java" +
                    " ======= token = " + token + ", transactionId = " + transactionId);
            return null;
        }
        if(lang == null || (lang != null && lang.isEmpty()))
            lang = DEFAULT_LANGUAGE;

        TransferInfo info = cinetpayTransferService.getTransferInfo(token, transactionId, lang);
        if(info == null) {
            log.error("[" + new Date() + "] => TRANSFER INFO NULL >>>>>>>> getTransferInformation :: MobileMoneyOperationsImpl.java");
            return null;
        }

        MobileMoneyTransferInfo transferInfo = new MobileMoneyTransferInfo();
        transferInfo.setTransactionId(info.getData().get(0).getTransaction_id());
        transferInfo.setAmount(info.getData().get(0).getAmount());
        transferInfo.setOperator(info.getData().get(0).getOperator());
        transferInfo.setPhone(info.getData().get(0).getReceiver_e164());
        transferInfo.setStatus(info.getData().get(0).getTreatment_status());
        transferInfo.setValidationDate(info.getData().get(0).getValidated_at());

        if(info.getData().get(0).getTreatment_status().equals(CinetpayParams.STATUS_TRANSFER_TREATMENT_VAL))
            transferInfo.setIsConfirmed(true);
        else if(info.getData().get(0).getTreatment_status().equals(CinetpayParams.STATUS_TRANSFER_TREATMENT_REJ))
            transferInfo.setIsInvalid(true);
        else {
            transferInfo.setIsConfirmed(false);
            transferInfo.setIsInvalid(false);
        }

        return transferInfo;
    }

}
