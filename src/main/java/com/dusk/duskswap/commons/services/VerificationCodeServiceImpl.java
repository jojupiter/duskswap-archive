package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.miscellaneous.Utilities;
import com.dusk.duskswap.commons.models.VerificationCode;
import com.dusk.duskswap.commons.repositories.VerificationCodeRepository;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Value("${signup.code.validity.inDays}")
    private int signupValidPeriodInDays;

    @Value("${signin.code.validity.inDays}")
    private int signinValidPeriodInDays;

    @Value("${withdrawal.code.validity.inMinutes}")
    private int withdrawalTime;

    @Override
    public VerificationCode createSigninCode(String emailOrUsername) {
        // input checking
        if(emailOrUsername == null || (emailOrUsername != null && emailOrUsername.isEmpty()))
            return null;
        // then check if user exists
        boolean doesUserExist = userRepository.existsByEmail(emailOrUsername) || userRepository.existsByUsername(emailOrUsername);
        if(!doesUserExist)
            return null;

        VerificationCode verificationCode = null;
        // First we get the corresponding user
        Optional<User> user = userRepository.existsByEmail(emailOrUsername) ?
                            userRepository.findByEmail(emailOrUsername) : userRepository.findByUsername(emailOrUsername);
        if(!user.isPresent())
            return null;

        // Next, we check if there already exists a valid code
        Optional<VerificationCode> lastSignInCode = verificationCodeRepository.findLastCreatedCodeByUserEmail(user.get().getEmail(),
                                                                                                              DefaultProperties.VERIFICATION_SIGNIN_PURPOSE);
        if(lastSignInCode.isPresent()) {
            if(Utilities.testVerificationCodeValidity(lastSignInCode.get()))
                return lastSignInCode.get();
        }

        // if no valid code found, just create one
        verificationCode = new VerificationCode();
        verificationCode.setCode(Utilities.generateVerificationCode());
        verificationCode.setPurpose(DefaultProperties.VERIFICATION_SIGNIN_PURPOSE);
        verificationCode.setUserEmail(user.get().getEmail());
        Date validityDate = new Date();
        validityDate.setTime(validityDate.getTime() + signinValidPeriodInDays * 24 * 3600 * 1000);
        verificationCode.setValidUntil(validityDate);

        return verificationCodeRepository.save(verificationCode);

    }

    @Override
    public VerificationCode createSignupCode(String email) {

        if(email == null || (email != null && email.isEmpty()))
            return null;
        VerificationCode verificationCode = null;

        // Next, we check if there already exists a valid code
        Optional<VerificationCode> lastSignInCode = verificationCodeRepository.findLastCreatedCodeByUserEmail(email,
                                                                                                              DefaultProperties.VERIFICATION_SIGNUP_PURPOSE);

        if(lastSignInCode.isPresent()) {
            if(Utilities.testVerificationCodeValidity(lastSignInCode.get()))
                return lastSignInCode.get();
        }

        // if no valid code found, just create one
        verificationCode = new VerificationCode();
        verificationCode.setCode(Utilities.generateVerificationCode());
        verificationCode.setPurpose(DefaultProperties.VERIFICATION_SIGNUP_PURPOSE);
        verificationCode.setUserEmail(email);
        Date validityDate = new Date();
        validityDate.setTime(validityDate.getTime() + signupValidPeriodInDays * 24 * 3600 * 1000);
        verificationCode.setValidUntil(validityDate);

        return verificationCodeRepository.save(verificationCode);
    }

    @Override
    public VerificationCode createWithdrawalCode(String email) {
        if(email == null || (email != null && email.isEmpty()))
            return null;
        VerificationCode verificationCode = null;

        // Next, we check if there already exists a valid code
        Optional<VerificationCode> lastSignInCode = verificationCodeRepository.findLastCreatedCodeByUserEmail(email,
                                                                                                              DefaultProperties.VERIFICATION_WITHDRAWAL_SELL_PURPOSE);

        if(lastSignInCode.isPresent()) {
            if(Utilities.testVerificationCodeValidity(lastSignInCode.get()))
                return lastSignInCode.get();
        }

        // if no valid code found, just create one
        verificationCode = new VerificationCode();
        verificationCode.setCode(Utilities.generateVerificationCode());
        verificationCode.setPurpose(DefaultProperties.VERIFICATION_WITHDRAWAL_SELL_PURPOSE);
        verificationCode.setUserEmail(email);
        Date validityDate = new Date();
        validityDate.setTime(validityDate.getTime() + withdrawalTime * 60 * 1000);
        verificationCode.setValidUntil(validityDate);

        return verificationCodeRepository.save(verificationCode);
    }

    @Override
    public Boolean isCodeCorrect(String email, Integer code, String purpose) {
        if(
                email == null || (email != null && email.isEmpty()) ||
                code == null ||
                purpose == null || (purpose != null && purpose.isEmpty())
        )
            return null;
        return verificationCodeRepository.existsByUserEmailAndCodeAndPurpose(email, code, purpose);
    }

    @Override
    public Boolean isCodeStillValid(String email, Integer code, String purpose) {
        if(code == null)
            return null;
        if(!verificationCodeRepository.existsByUserEmailAndCodeAndPurpose(email, code, purpose))
            return false;
        List<VerificationCode> verificationCodes = verificationCodeRepository.findByCode(code);
        for (VerificationCode verificationCode : verificationCodes)
            if(Utilities.testVerificationCodeValidity(verificationCode))
                return true;
        return false;
    }
}
