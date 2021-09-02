package com.dusk.externalAPIs.blockstream.services;

import com.dusk.externalAPIs.apiInterfaces.interfaces.CryptoOperations;
import com.dusk.externalAPIs.apiInterfaces.models.TransactionInfos;
import org.springframework.stereotype.Service;

@Service
public class BlockStreamService implements CryptoOperations {


    @Override
    public TransactionInfos checkTransaction(String transactionId) {
        return null;
    }
}
