package com.dusk.duskswap.commons.services;


import com.dusk.duskswap.commons.models.VerificationCode;

public interface VerificationCodeService {
    // create methods are separated to allow independent customization
    VerificationCode createSigninCode(String emailOrUsername);
    VerificationCode createSignupCode(String email);
    VerificationCode createWithdrawalCode(String email);
    Boolean isCodeCorrect(String email, Integer code, String purpose);
    Boolean isCodeStillValid(String email, Integer code, String purpose);
}
