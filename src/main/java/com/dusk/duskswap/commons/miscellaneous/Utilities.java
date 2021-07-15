package com.dusk.duskswap.commons.miscellaneous;

import java.util.Random;

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

}
