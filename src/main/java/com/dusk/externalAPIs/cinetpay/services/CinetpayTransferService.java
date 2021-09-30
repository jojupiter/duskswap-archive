package com.dusk.externalAPIs.cinetpay.services;

import com.dusk.externalAPIs.cinetpay.models.Contact;
import com.dusk.externalAPIs.cinetpay.models.TransferBalance;
import com.dusk.externalAPIs.cinetpay.models.TransferInfo;

public interface CinetpayTransferService {

    String getAuthToken(String lang, String apikey, String password);
    TransferBalance getTransferBalance(String token, String lang);
    TransferInfo transferMoney(String token, String lang, Contact contact);

}
