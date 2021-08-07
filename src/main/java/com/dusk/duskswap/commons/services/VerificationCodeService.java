package com.dusk.duskswap.commons.services;


import com.dusk.duskswap.commons.models.VerificationCode;

public interface VerificationCodeService {
    VerificationCode createSigninCode(String emailOrUsername);
    VerificationCode createSignupCode(String email);
    VerificationCode createWithdrawalCode(String email);
    VerificationCode createForgotPasswordCode(String email);
    Boolean isCodeCorrect(String email, Integer code, String purpose);
    Boolean updateCode(String email, String purpose);
    /*Boolean isCodeStillValid(String email, Integer code, String purpose);*/
}
