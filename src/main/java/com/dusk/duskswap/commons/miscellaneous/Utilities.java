package com.dusk.duskswap.commons.miscellaneous;

import com.dusk.duskswap.commons.models.InvoicePayment;
import com.dusk.duskswap.commons.models.Payment;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Random;

@Slf4j
public class Utilities {

    public static int generateVerificationCode() {
        return new Random().nextInt(900000) + 100000;
    }

    public static Double convertUSdtToXaf(Double cryptoAmount, Double unitPriceCryptoUsdt, Double unitUsdtToEur) {

        Double conversionInXaf = cryptoAmount *
                                 unitPriceCryptoUsdt * // price in usdt
                                 unitUsdtToEur *  // price in Eur
                                 Double.parseDouble(DefaultProperties.PRICING_EURO_DEFAULT_VALUE_IN_XAF); // price in Xaf

        return Math.floor(conversionInXaf);
    }

    public static Double convertXafToCrypto(Double priceInXaf, Double unitCryptoToUsdt, Double eurToUsdt) {

        Double conversionInCrypto = priceInXaf /
                                    Double.parseDouble(DefaultProperties.PRICING_EURO_DEFAULT_VALUE_IN_XAF) / // to Euro
                                    eurToUsdt / // to Usdt
                                    unitCryptoToUsdt; // to crypto

        return conversionInCrypto;
    }

    public static Payment findPayment(List<InvoicePayment> invoicePayments, String transactionHash) {
        // input checking
        if(
                invoicePayments == null || (invoicePayments != null && invoicePayments.isEmpty()) ||
                transactionHash == null || (transactionHash != null && transactionHash.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> findPayment :: Utilities.java");
            return null;
        }

        // we check payment from last to first in invoicePayments
        Payment foundPayment = null;

        for(InvoicePayment invoicePayment: invoicePayments) {
            for(int i = invoicePayment.getPayments().size() - 1; i >= 0; i--) {
                if(invoicePayment.getPayments().get(i).getDestination().equals(transactionHash)) {
                    foundPayment = invoicePayment.getPayments().get(i);
                    break;
                }
            }
        }

        return foundPayment;
    }

    public static Double estimateNetworkFees(String cryptoIso) {
        // input checking
        if(cryptoIso == null || (cryptoIso != null && cryptoIso.isEmpty())) {
            log.error("[" + new Date() + "] => cryptoIso NULL OR EMPTY >>>>>>>> estimateNetworkFees :: Utilities.java");
            return null;
        }

        if(cryptoIso.toLowerCase().equals("btc") || cryptoIso.toLowerCase().equals("bitcoin")) {
            return DefaultProperties.MAX_BTC_SAT_PER_BYTES *
                   DefaultProperties.BTC_TRANSACTION_SIZE_MAX *
                   Math.pow(10, -8);
        }

        return null;
    }

    public static Boolean checkNetworkConfirmations(String cryptoIso, Long nConfirmations) {
        if(
                cryptoIso == null || (cryptoIso != null && cryptoIso.isEmpty()) ||
                nConfirmations == null
        ) {
            log.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> checkNetworkConfirmations :: Utilities.java " +
                    "====== cryptoIso = " + cryptoIso + ", nConfirmations = " + nConfirmations);
            return null;
        }

        if(
                (cryptoIso.toLowerCase().equals("btc") || cryptoIso.toLowerCase().equals("bitcoin")) &&
                nConfirmations >= DefaultProperties.BTC_REQUIRED_CONFIRMATIONS
        )
            return true;
        if(
                (cryptoIso.toLowerCase().equals("eth") || cryptoIso.toLowerCase().equals("ethereum")) &&
                nConfirmations >= DefaultProperties.ETH_REQUIRED_CONFIRMATIONS
        )
            return true;
        if(
                (cryptoIso.toLowerCase().equals("ltc") || cryptoIso.toLowerCase().equals("litecoin")) &&
                nConfirmations >= DefaultProperties.LTC_REQUIRED_CONFIRMATIONS
        )
            return true;
        if(cryptoIso.toLowerCase().equals("dash") && nConfirmations >= DefaultProperties.DASH_REQUIRED_CONFIRMATIONS)
            return true;
        if(
                (cryptoIso.toLowerCase().equals("doge") || cryptoIso.toLowerCase().equals("dogecoin")) &&
                nConfirmations >= DefaultProperties.DOGE_REQUIRED_CONFIRMATIONS
        )
            return true;
        if(
                (cryptoIso.toLowerCase().equals("shib") || cryptoIso.toLowerCase().equals("shiba inu")) &&
                nConfirmations >= DefaultProperties.SHIB_REQUIRED_CONFIRMATIONS
        )
            return true;

        return false;
    }


    public static String extractTransactionHash(String btcpayTxId, String cryptoIso) {
        if(
                cryptoIso == null || (cryptoIso != null && cryptoIso.isEmpty()) ||
                btcpayTxId == null || (btcpayTxId != null && btcpayTxId.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> extractTransactionHash :: Utilities.java " +
                    "====== cryptoIso = " + cryptoIso + ", btcpayTxId = " + btcpayTxId);
            return null;
        }

        String hash = null;

        if(cryptoIso.toLowerCase().equals("btc") || cryptoIso.toLowerCase().equals("bitcoin"))
            hash = btcpayTxId.split("-")[0];
        if(cryptoIso.toLowerCase().equals("xmr") || cryptoIso.toLowerCase().equals("monero"))
            hash = btcpayTxId.split(":")[0];
        return hash;
    }

}
