package com.dusk.externalAPIs.apiInterfaces.interfaces;

import com.dusk.externalAPIs.apiInterfaces.models.TransactionInfos;
import com.dusk.externalAPIs.blockstream.models.Transaction;
import com.dusk.externalAPIs.blockstream.services.BlockStreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class BlockExplorerOperationsImpl implements  BlockExplorerOperations {

    @Override
    public TransactionInfos getTransaction(String txId, String cryptoIso) {
        // input checking
        if(
                txId == null || (txId != null && txId.isEmpty()) ||
                cryptoIso == null || (cryptoIso != null && cryptoIso.isEmpty())
        ) {
            log.error("[" + new Date() + "] => txId null >>>>>>>> BlockExplorerOperationsImpl :: BlockStreamService.java" +
                    " ===== txId = " + txId + ", cryptoIso = " + cryptoIso);
            return null;
        }

        if(cryptoIso.toLowerCase().equals("btc") || cryptoIso.toLowerCase().equals("bitcoin")) { // for bitcoin
            Transaction transaction = null;
            transaction = BlockStreamService.getTransaction(txId);
            if(transaction != null) {
                TransactionInfos transactionInfos = new TransactionInfos();
                transactionInfos.setCryptoIso(cryptoIso);
                transactionInfos.setFees(transaction.getFee() * Math.pow(10, -8)); // in btc
                transactionInfos.setInAddress(transaction.getVin().get(0).getPrevout().getScriptpubkey_address());
                transactionInfos.setOutAddress(transaction.getVout().get(0).getScriptpubkey_address());

                // now we calculate the number of confirmation
                if(transaction.getStatus().getConfirmed() != null && transaction.getStatus().getConfirmed()) {
                    Long confirmations = 0L;
                    Long blocksHeight = BlockStreamService.getBlocksHeight();
                    transactionInfos.setNConfirmations(blocksHeight - transaction.getStatus().getBlock_height() + 1);
                }

                return transactionInfos;
            }
        }

        return null;
    }

    @Override
    public Double getEstimatedFees(String cryptoIso, Integer blockTarget) {
        // input checking
        if(cryptoIso == null || (cryptoIso != null && cryptoIso.isEmpty()) || blockTarget == 0) {
            log.error("[" + new Date() + "] => txId null >>>>>>>> BlockExplorerOperationsImpl :: BlockStreamService.java");
            return null;
        }

        // According to each cryptoIso, we apply a method
        if(cryptoIso.toLowerCase().equals("btc") || cryptoIso.toLowerCase().equals("bitcoin")) {// for bitcoin
            Map<String, String> fees = BlockStreamService.getFeesEstimation();
            for(int i = blockTarget; i >= 1; i--) {
                if(fees.containsKey(Integer.toString(i)))
                    return Double.parseDouble(fees.get(Integer.toString(i))) * Math.pow(10, -8); // in btc/vb
            }
        }
        return null;
    }
}
