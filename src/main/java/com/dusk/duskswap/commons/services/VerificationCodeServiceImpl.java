package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.miscellaneous.Utilities;
import com.dusk.duskswap.commons.models.VerificationCode;
import com.dusk.duskswap.commons.repositories.VerificationCodeRepository;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService{

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;
    private Logger logger = LoggerFactory.getLogger(VerificationCodeServiceImpl.class);

    @Override
    public VerificationCode createSigninCode(String emailOrUsername) {
        // input checking
        if(emailOrUsername == null || (emailOrUsername != null && emailOrUsername.isEmpty())) {
            logger.error("EMAIL EMPTY OR NULL >>>>>>>> createSignInCode :: VerificationCodeServiceImpl.java ==== email = " + emailOrUsername);
            return null;
        }
        // then check if user exists
        boolean doesUserExist = userRepository.existsByEmail(emailOrUsername) || userRepository.existsByUsername(emailOrUsername);
        if(!doesUserExist) {
            logger.error("USER DOESN'T EXISTS >>>>>>>> createSignInCode :: VerificationCodeServiceImpl.java");
            return null;
        }

        // First we get the corresponding user
        Optional<User> user = userRepository.existsByEmail(emailOrUsername) ?
                            userRepository.findByEmail(emailOrUsername) : userRepository.findByUsername(emailOrUsername);
        if(!user.isPresent()) {
            logger.error("USER DOESN'T EXISTS (result null) >>>>>>>> createSignInCode :: VerificationCodeServiceImpl.java");
            return null;
        }

        // then we create/update the code.
        Optional<VerificationCode> currentVerificationCode = verificationCodeRepository.findByUserEmailAndPurpose(emailOrUsername, DefaultProperties.VERIFICATION_SIGN_IN_UP_PURPOSE);

        if(currentVerificationCode.isPresent()) {
            currentVerificationCode.get().setCode(Utilities.generateVerificationCode());
            verificationCodeRepository.save(currentVerificationCode.get());
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
            logger.error("INPUT NULL >>>>>>>> createSignUpCode :: VerificationCodeServiceImpl.java ==== email = " + email);
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
            logger.error("EMAIL EMPTY OR NULL >>>>>>>> createWithdrawalCode :: VerificationCodeServiceImpl.java ==== email = " + email);
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
    public Boolean isCodeCorrect(String email, Integer code, String purpose) {
        if(
                email == null || (email != null && email.isEmpty()) ||
                code == null ||
                purpose == null || (purpose != null && purpose.isEmpty())
        ) {
            logger.error("INPUT NULL OR EMPTY >>>>>>>> createWithdrawalCode :: VerificationCodeServiceImpl.java ===== " +
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
            logger.error("INPUT NULL OR EMPTY >>>>>>>> updateCode :: VerificationCodeServiceImpl.java ===== " +
                    "email = " + email + ", purpose = " + purpose);
            return false;
        }

        Optional<VerificationCode> code = verificationCodeRepository.findByUserEmailAndPurpose(email, purpose);
        if(!code.isPresent()) {
            logger.error("OLD CODE NOT FOUND >>>>>>>> updateCode :: VerificationCodeServiceImpl.java");
            return false;
        }

        code.get().setCode(Utilities.generateVerificationCode());

        verificationCodeRepository.save(code.get());

        return true;
    }

}
