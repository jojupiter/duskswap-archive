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
            log.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> createDeposit :: DepositServiceImpl.java");
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
            log.error("[" + new Date() + "] => cryptoIso NULL OR EMPTY >>>>>>>> estimateNetworkFees :: DepositServiceImpl.java");
            return null;
        }

        if(cryptoIso.toLowerCase().equals("btc") || cryptoIso.toLowerCase().equals("bitcoin")) {
            return DefaultProperties.MAX_BTC_SAT_PER_BYTES *
                   DefaultProperties.BTC_TRANSACTION_SIZE_MAX *
                   Math.pow(10, -8);
        }

        return null;
    }

}
