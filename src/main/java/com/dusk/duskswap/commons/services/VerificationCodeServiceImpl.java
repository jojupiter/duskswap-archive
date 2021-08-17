package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.miscellaneous.Utilities;
import com.dusk.duskswap.commons.models.VerificationCode;
import com.dusk.duskswap.commons.repositories.VerificationCodeRepository;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService{

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Override
    public VerificationCode createSigninCode(String emailOrUsername) {
        // input checking
        if(emailOrUsername == null || (emailOrUsername != null && emailOrUsername.isEmpty())) {
            log.error("EMAIL EMPTY OR NULL >>>>>>>> createSignInCode :: VerificationCodeServiceImpl.java ==== email = " + emailOrUsername);
            return null;
        }
        // then check if user exists
        boolean doesUserExist = userRepository.existsByEmail(emailOrUsername) || userRepository.existsByUsername(emailOrUsername);
        if(!doesUserExist) {
            log.error("USER DOESN'T EXISTS >>>>>>>> createSignInCode :: VerificationCodeServiceImpl.java");
            return null;
        }

        // First we get the corresponding user
        Optional<User> user = userRepository.existsByEmail(emailOrUsername) ?
                            userRepository.findByEmail(emailOrUsername) : userRepository.findByUsername(emailOrUsername);
        if(!user.isPresent()) {
            log.error("USER DOESN'T EXISTS (result null) >>>>>>>> createSignInCode :: VerificationCodeServiceImpl.java");
            return null;
        }

        // then we create/update the code.
        Optional<VerificationCode> currentVerificationCode = verificationCodeRepository.findByUserEmailAndPurpose(emailOrUsername, DefaultProperties.VERIFICATION_SIGN_IN_UP_PURPOSE);

        if(currentVerificationCode.isPresent()) {
            currentVerificationCode.get().setCode(Utilities.generateVerificationCode());
            return verificationCodeRepository.save(currentVerificationCode.get());
        }
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode(Utilities.generateVerificationCode());
        verificationCode.setPurpose(DefaultProperties.VERIFICATION_SIGN_IN_UP_PURPOSE);
        verificationCode.setUserEmail(user.get().getEmail());

        return verificationCodeRepository.save(verificationCode);

    }

    @Override
    public VerificationCode createSignupCode(String email) {

        if(email == null || (email != null && email.isEmpty())) {
            log.error("INPUT NULL >>>>>>>> createSignUpCode :: VerificationCodeServiceImpl.java ==== email = " + email);
            return null;
        }
        VerificationCode verificationCode = null;

        // then we create/update the code.
        verificationCode = new VerificationCode();
        verificationCode.setCode(Utilities.generateVerificationCode());
        verificationCode.setPurpose(DefaultProperties.VERIFICATION_SIGN_IN_UP_PURPOSE);
        verificationCode.setUserEmail(email);

        return verificationCodeRepository.save(verificationCode);
    }

    @Override
    public VerificationCode createWithdrawalCode(String email) {
        if(email == null || (email != null && email.isEmpty())) {
            log.error("EMAIL EMPTY OR NULL >>>>>>>> createWithdrawalCode :: VerificationCodeServiceImpl.java ==== email = " + email);
            return null;
        }

        // then we create/update the code.
        Optional<VerificationCode> currentVerificationCode = verificationCodeRepository.findByUserEmailAndPurpose(email, DefaultProperties.VERIFICATION_WITHDRAWAL_SELL_PURPOSE);

        if(currentVerificationCode.isPresent()) {
            currentVerificationCode.get().setCode(Utilities.generateVerificationCode());
            return verificationCodeRepository.save(currentVerificationCode.get());
        }
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode(Utilities.generateVerificationCode());
        verificationCode.setPurpose(DefaultProperties.VERIFICATION_WITHDRAWAL_SELL_PURPOSE);
        verificationCode.setUserEmail(email);

        return verificationCodeRepository.save(verificationCode);
    }

    @Override
    public VerificationCode createForgotPasswordCode(String email) {
        if(email == null || (email != null && email.isEmpty())) {
            log.error("EMAIL EMPTY OR NULL >>>>>>>> createForgotPasswordCode :: VerificationCodeServiceImpl.java ==== email = " + email);
            return null;
        }

        Optional<VerificationCode> currentVerificationCode = verificationCodeRepository.findByUserEmailAndPurpose(email, DefaultProperties.VERIFICATION_FORGOT_PASSWORD);

        if(currentVerificationCode.isPresent()) {
            currentVerificationCode.get().setCode(Utilities.generateVerificationCode());
            return verificationCodeRepository.save(currentVerificationCode.get());
        }
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode(Utilities.generateVerificationCode());
        verificationCode.setPurpose(DefaultProperties.VERIFICATION_FORGOT_PASSWORD);
        verificationCode.setUserEmail(email);

        return verificationCodeRepository.save(verificationCode);
    }

    @Override
    public Boolean isCodeCorrect(String email, Integer code, String purpose) {
        if(
                email == null || (email != null && email.isEmpty()) ||
                code == null ||
                purpose == null || (purpose != null && purpose.isEmpty())
        ) {
            log.error("INPUT NULL OR EMPTY >>>>>>>> createWithdrawalCode :: VerificationCodeServiceImpl.java ===== " +
                    "email = " + email + ", code = " + code + ", purpose = " + purpose);
            return null;
        }
        return verificationCodeRepository.existsByUserEmailAndCodeAndPurpose(email, code, purpose);
    }

    @Override
    public Boolean updateCode(String email, String purpose) {
        // input checking
        if(
                email == null || (email != null && email.isEmpty()) ||
                purpose == null || (purpose != null && purpose.isEmpty())
        ) {
            log.error("INPUT NULL OR EMPTY >>>>>>>> updateCode :: VerificationCodeServiceImpl.java ===== " +
                    "email = " + email + ", purpose = " + purpose);
            return false;
        }

        Optional<VerificationCode> code = verificationCodeRepository.findByUserEmailAndPurpose(email, purpose);
        if(!code.isPresent()) {
            log.error("OLD CODE NOT FOUND >>>>>>>> updateCode :: VerificationCodeServiceImpl.java");
            return false;
        }

        code.get().setCode(Utilities.generateVerificationCode());

        verificationCodeRepository.save(code.get());

        return true;
    }

}
