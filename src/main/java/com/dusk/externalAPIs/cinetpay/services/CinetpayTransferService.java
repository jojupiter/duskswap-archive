package com.dusk.externalAPIs.cinetpay.services;

import com.dusk.externalAPIs.cinetpay.models.Contact;
import com.dusk.externalAPIs.cinetpay.models.TransferBalance;
import com.dusk.externalAPIs.cinetpay.models.TransferInfo;

import java.util.List;

public interface CinetpayTransferService {

    String getAuthToken(String lang, String apikey, String password);
    TransferBalance getTransferBalance(String token, String lang);
    TransferInfo transferMoney(String token, String lang, List<Contact> contacts);
    List<Contact> addContacts(String token, String lang, List<Contact> contacts);

}
