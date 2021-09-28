package com.dusk.externalAPIs.apiInterfaces.interfaces;

import com.dusk.externalAPIs.apiInterfaces.models.TransactionInfos;

public interface BlockExplorerOperations {

    TransactionInfos getTransaction(String transactionId, String cryptoIso);
    Double getEstimatedFees(String cryptoIso, Double feeRate);
    Double getEstimatedFeeRate(String cryptoIso);

}
