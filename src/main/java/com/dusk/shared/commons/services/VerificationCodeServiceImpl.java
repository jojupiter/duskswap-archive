package com.dusk.shared.commons.services;

import com.dusk.shared.commons.miscellaneous.Utilities;
import com.dusk.shared.commons.models.VerificationCode;
import com.dusk.shared.commons.repositories.VerificationCodeRepository;
import com.dusk.shared.usersManagement.models.User;
import com.dusk.shared.usersManagement.repositories.UserRepository;
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

    private Utilities utilities = new Utilities();

    @Override
    public VerificationCode createSigninCode(String emailOrUsername) {
        boolean doesUserExist = userRepository.existsByEmail(emailOrUsername) || userRepository.existsByUsername(emailOrUsername);
        if(doesUserExist) {
            VerificationCode verificationCode = null;
            // First we get the corresponding user
            Optional<User> user = userRepository.existsByEmail(emailOrUsername) ?
                    userRepository.findByEmail(emailOrUsername) : userRepository.findByUsername(emailOrUsername);
            if(!user.isPresent())
                return null;

            // Next, we check if there already exists a valid code
            List<VerificationCode> verificationCodes = verificationCodeRepository.findByUserEmail(user.get().getEmail());
            if(verificationCodes != null)
                for(VerificationCode code: verificationCodes) {
                    if(utilities.testVerificationCodeValidity(code))
                        return code;
                }

            // if no valid code found, just create one
            verificationCode = new VerificationCode();
            verificationCode.setCode(utilities.generateVerificationCode());
            verificationCode.setPurpose("SIGN_IN");
            verificationCode.setUserEmail(user.get().getEmail());
            Date validityDate = new Date();
            validityDate.setTime(validityDate.getTime() + signinValidPeriodInDays * 24 * 3600 * 1000);
            verificationCode.setValidUntil(validityDate);

            return verificationCodeRepository.save(verificationCode);
        }
        return null;
    }

    @Override
    public VerificationCode createSignupCode(String email) {
            VerificationCode verificationCode = null;

            // Next, we check if there already exists a valid code
            List<VerificationCode> verificationCodes = verificationCodeRepository.findByUserEmail(email);
            if(verificationCodes != null)
                for(VerificationCode code: verificationCodes) {
                    if(utilities.testVerificationCodeValidity(code))
                        return code;
                }

            // if no valid code found, just create one
            verificationCode = new VerificationCode();
            verificationCode.setCode(utilities.generateVerificationCode());
            verificationCode.setPurpose("SIGN_UP");
            verificationCode.setUserEmail(email);
            Date validityDate = new Date();
            validityDate.setTime(validityDate.getTime() + signupValidPeriodInDays * 24 * 3600 * 1000);
            verificationCode.setValidUntil(validityDate);

            return verificationCodeRepository.save(verificationCode);
    }

    @Override
    public Boolean isCodeCorrect(String email, Integer code) {
        return verificationCodeRepository.existsByUserEmailAndCode(email, code);
        // return verificationCodeRepository.existsByCode(code) && verificationCodeRepository.existsByUserEmail(email);
    }

    @Override
    public Boolean isCodeStillValid(Integer code) {
        if(!verificationCodeRepository.existsByCode(code))
            return false;
        List<VerificationCode> verificationCodes = verificationCodeRepository.findByCode(code);
        for (VerificationCode verificationCode : verificationCodes)
            if(utilities.testVerificationCodeValidity(verificationCode))
                return true;
        return false;
    }
}
