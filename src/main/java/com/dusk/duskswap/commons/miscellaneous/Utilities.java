package com.dusk.duskswap.commons.miscellaneous;


import com.dusk.duskswap.commons.models.VerificationCode;

import java.util.Date;
import java.util.Random;

public class Utilities {

    public static int generateVerificationCode() {
        return new Random().nextInt(900000) + 100000;
    }

    public static boolean testVerificationCodeValidity(VerificationCode code) {
        return code.getValidUntil().after(new Date());
    }

}
