package com.dusk.shared.commons.services;


import com.dusk.shared.commons.models.VerificationCode;

public interface VerificationCodeService {
    VerificationCode createSigninCode(String emailOrUsername);
    VerificationCode createSignupCode(String email);
    Boolean isCodeCorrect(String email, Integer code);
    Boolean isCodeStillValid(Integer code);
}
