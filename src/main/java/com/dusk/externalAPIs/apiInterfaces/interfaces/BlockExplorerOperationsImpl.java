package com.dusk.externalAPIs.apiInterfaces.interfaces;

import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.externalAPIs.apiInterfaces.models.CryptoTransactionInfo;
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
    public CryptoTransactionInfo getTransaction(String txId, String cryptoIso) {
        // input checking
        if(
                txId == null || (txId != null && txId.isEmpty()) ||
                cryptoIso == null || (cryptoIso != null && cryptoIso.isEmpty())
        ) {
            log.error("[" + new Date() + "] => txId null >>>>>>>> getTransaction :: BlockExplorerOperationsImpl.java" +
                    " ===== txId = " + txId + ", cryptoIso = " + cryptoIso);
            return null;
        }

        if(cryptoIso.toLowerCase().equals("btc") || cryptoIso.toLowerCase().equals("bitcoin")) { // for bitcoin
            Transaction transaction = null;
            transaction = BlockStreamService.getTransaction(txId);
            if(transaction != null) {
                CryptoTransactionInfo cryptoTransactionInfo = new CryptoTransactionInfo();
                cryptoTransactionInfo.setCryptoIso(cryptoIso);
                cryptoTransactionInfo.setFees(transaction.getFee() * Math.pow(10, -8)); // in btc
                cryptoTransactionInfo.setInAddress(transaction.getVin().get(0).getPrevout().getScriptpubkey_address());
                cryptoTransactionInfo.setOutAddress(transaction.getVout().get(0).getScriptpubkey_address());

                // now we calculate the number of confirmation
                if(transaction.getStatus().getConfirmed() != null && transaction.getStatus().getConfirmed()) {
                    Long confirmations = 0L;
                    Long blocksHeight = BlockStreamService.getBlocksHeight();
                    cryptoTransactionInfo.setNConfirmations(blocksHeight - transaction.getStatus().getBlock_height() + 1);
                }

                return cryptoTransactionInfo;
            }
        }

        return null;
    }

    @Override
    public Double getEstimatedFees(String cryptoIso, Double feeRate) {
        // input checking
        if(
                cryptoIso == null || (cryptoIso != null && cryptoIso.isEmpty()) ||
                feeRate == null || (feeRate != null && feeRate == 0)
        ) {
            log.error("[" + new Date() + "] => txId null >>>>>>>> getEstimatedFees :: BlockExplorerOperationsImpl.java");
            return null;
        }

        // According to each cryptoIso, we apply a method
        if(cryptoIso.toLowerCase().equals("btc") || cryptoIso.toLowerCase().equals("bitcoin")) {// for bitcoin
                return  feeRate *
                        Math.pow(10, -8) *
                        DefaultProperties.BTC_TRANSACTION_SIZE_MAX; // in btc
        }
        return null;
    }

    @Override
    public Double getEstimatedFeeRate(String cryptoIso) {
        // input checking
        if(cryptoIso == null || (cryptoIso != null && cryptoIso.isEmpty())) {
            log.error("[" + new Date() + "] => txId null >>>>>>>> getEstimatedFeeRate :: BlockExplorerOperationsImpl.java");
            return null;
        }

        // According to each cryptoIso, we apply a method
        if(cryptoIso.toLowerCase().equals("btc") || cryptoIso.toLowerCase().equals("bitcoin")) {// for bitcoin
            Map<String, String> fees = BlockStreamService.getFeesEstimation();
            if(fees == null) {
                return DefaultProperties.MAX_BTC_SAT_PER_BYTES;
            }
            for(int i = DefaultProperties.BTC_REQUIRED_CONFIRMATIONS; i >= 1; i--) {
                if(fees.containsKey(Integer.toString(i))) {
                    if(Double.parseDouble(fees.get(Integer.toString(i))) <= 0)
                        return DefaultProperties.MAX_BTC_SAT_PER_BYTES;
                    return Math.min(
                            Double.parseDouble(fees.get(Integer.toString(i))),
                            DefaultProperties.MAX_BTC_SAT_PER_BYTES
                    ); // in sat/vb
                }
            }
            // if no value found, just return the default max btc sat/b value
            return DefaultProperties.MAX_BTC_SAT_PER_BYTES;
        }

        return null;
    }
}
