package com.dusk.shared.commons.miscellaneous;


import com.dusk.shared.commons.models.VerificationCode;

import java.util.Date;
import java.util.Random;

public class Utilities {

    public int generateVerificationCode() {
        return new Random().nextInt(900000) + 100000;
    }

    public boolean testVerificationCodeValidity(VerificationCode code) {
        return code.getValidUntil().after(new Date());
    }

}
