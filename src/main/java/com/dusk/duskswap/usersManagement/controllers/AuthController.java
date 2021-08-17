package com.dusk.duskswap.usersManagement.controllers;

import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.mailing.models.Email;
import com.dusk.duskswap.commons.mailing.services.EmailService;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.miscellaneous.FieldValidation;
import com.dusk.duskswap.commons.models.JwtResponse;
import com.dusk.duskswap.commons.models.VerificationCode;
import com.dusk.duskswap.commons.services.VerificationCodeService;
import com.dusk.duskswap.newslettersManagement.services.NewsLetterService;
import com.dusk.duskswap.usersManagement.models.*;
import com.dusk.duskswap.usersManagement.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private NewsLetterService newsLetterService;
    private FieldValidation fieldValidation = new FieldValidation();


    @PostMapping(value = "/signin", produces = "application/json")
    public ResponseEntity<?> authenticateUser(@RequestParam(name = "code") Integer code,
                                              @RequestParam(name = "email") String email,
                                              @RequestParam(name = "password") String password) {

        // Inputs verification
        if(
                (email.isEmpty() || email == null) ||
                (password.isEmpty() || password == null) ||
                code == null
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> authenticateUser :: AuthController.java" +
                    "===== email = " + email + ", password = " + password + ", code = " + code);
            return ResponseEntity.badRequest().body(null);
        }

        HashMap<String, String> emailValidity = fieldValidation.isEmailValid(email);
        if(!Boolean.parseBoolean(emailValidity.get("isEmailValid"))) {
            log.error("[" + new Date() + "] => EMAIL INVALID >>>>>>>> authenticateUser :: AuthController.java");
            return ResponseEntity
                    .badRequest()
                    .body(emailValidity.get("message"));
        }

        HashMap<String, String> passValidity = fieldValidation.isPasswordValid(password);
        if(!Boolean.parseBoolean(passValidity.get("isPasswordValid"))) {
            log.error("[" + new Date() + "] => PASSWORD INVALID >>>>>>>> authenticateUser :: AuthController.java");
            return ResponseEntity
                    .badRequest()
                    .body(passValidity.get("message"));
        }

        // First we need to verify if the supplied code is correct and valid
        if(!verificationCodeService.isCodeCorrect(email, code, DefaultProperties.VERIFICATION_SIGN_IN_UP_PURPOSE)) {
            log.error("[" + new Date() + "] => VERIFICATION CODE INVALID >>>>>>>> authenticateUser :: AuthController.java");
            return ResponseEntity
                    .badRequest()
                    .body("The verification code is not correct/valid");
        }


        // if the code is correct, then proceed to authentication
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        if(userService.existsByEmail(email)) {
            Optional<User> user = userService.getUserByEmail(email);
            if(user.isPresent()) {
                user.get().setLastLogin(new Date());
                userService.addUser(user.get());
            }
        }

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping(value = "/signup", produces = "application/json")
    public ResponseEntity<?> registerUser(
                                            @RequestParam(name = "verification_code") Integer code,
                                            @Valid @RequestBody SignupRequest signupRequest
                                          ) {
        // Input verification
        if(signupRequest == null || code == null) {
            log.error("[" + new Date() + "] => INPUT INVALID >>>>>>>> registerUser :: AuthController.java" +
                    " ==== signuprequest = " + signupRequest + ", code = " + code);
            return ResponseEntity.badRequest().body(null);
        }

        if (userService.existsByUsername(signupRequest.getEmail())) {
            log.error("[" + new Date() + "] => Error: Username is already taken! >>>>>>>> registerUser :: AuthController.java");
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        if (userService.existsByEmail(signupRequest.getEmail())){
            log.error("[" + new Date() + "] => Error: Email is already in use! >>>>>>>> registerUser :: AuthController.java");
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        HashMap<String, String> emailValidity = fieldValidation.isEmailValid(signupRequest.getEmail());
        if(!Boolean.parseBoolean(emailValidity.get("isEmailValid"))) {
            log.error("[" + new Date() + "] => EMAIL INVALID (" + emailValidity.get("message") + ") >>>>>>>> registerUser :: AuthController.java");
            return ResponseEntity
                    .badRequest()
                    .body(emailValidity.get("message"));
        }

        HashMap<String, String> passValidity = fieldValidation.isPasswordValid(signupRequest.getPassword());
        if(!Boolean.parseBoolean(passValidity.get("isPasswordValid"))) {
            log.error("[" + new Date() + "] => PASSWORD INVALID (" + passValidity.get("message") + ") >>>>>>>> registerUser :: AuthController.java");
            return ResponseEntity
                    .badRequest()
                    .body(passValidity.get("message"));
        }

        // Check if verification code is correct and valid
        if(!verificationCodeService.isCodeCorrect(signupRequest.getEmail(), code, DefaultProperties.VERIFICATION_SIGN_IN_UP_PURPOSE)) {
            log.error("[" + new Date() + "] => PASSWORD INVALID (" + passValidity.get("message") + ") >>>>>>>> registerUser :: AuthController.java");
            return ResponseEntity
                    .badRequest()
                    .body("The verification code is not correct/valid");
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signupRequest.getEmail());
        user.setEmail(signupRequest.getEmail());
        user.setEncryptedPassword(encoder.encode(signupRequest.getPassword()));
        user.setRoles(new HashSet<Role>());
        Optional<Role> roleUser = userService.getRoleByName(DefaultProperties.ROLE_USER);
        if(roleUser.isPresent()) {
            user.getRoles().add(roleUser.get());
        }
        else {
            log.error("NO ROLE USER FOUND>>>>>>>> registerUser :: AuthController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        User addedUser = userService.addUser(user);

        newsLetterService.addSubscriberToNewsLetter(signupRequest.getEmail());

        return ResponseEntity.ok(addedUser);
    }

    @PostMapping(value = "/signin/ask-code", produces = "application/json")
    public ResponseEntity<?> askForSigninVerificationCode(@RequestParam(name = "email") String email) {

        // Input verification
        if (email.isEmpty() || email == null) {
            log.error("[" + new Date() + "] => EMAIL NULL OR EMPTY >>>>>>>> askForSigninVerificationCode :: AuthController.java");
            return ResponseEntity.
                    badRequest().
                    body("Email null or empty");
        }

        if (!userService.existsByEmail(email)) { // if user doesn't exist, then return null
            log.error("[" + new Date() + "] => USER DOESN'T EXIST >>>>>>>> askForSigninVerificationCode :: AuthController.java");
            return new ResponseEntity<>("User doesn't exist", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        HashMap<String, String> emailValidity = fieldValidation.isEmailValid(email);
        if(!Boolean.parseBoolean(emailValidity.get("isEmailValid"))) {
            log.error("[" + new Date() + "] => EMAIL IS INVALID (" +emailValidity.get("message")+ ") >>>>>>>> askForSigninVerificationCode :: AuthController.java");
            return ResponseEntity
                    .badRequest()
                    .body(emailValidity.get("message"));
        }
        // Here we create the verification code
        VerificationCode verificationCode = verificationCodeService.createSigninCode(email);

        // Once the code is created, we send it to the user via his email address
        Email emailMessage = new Email();
        emailMessage.setMessage(Integer.toString(verificationCode.getCode()));
        List<String> toAddresses = new ArrayList<>();
        toAddresses.add(email);
        emailMessage.setTo(toAddresses);

        emailService.sendSigninConfirmationEmail(emailMessage);

        return ResponseEntity.ok(true);

    }

    @PostMapping(value = "/signup/ask-code", produces = "application/json")
    public ResponseEntity<?> askRegistrationCode(@Valid @RequestBody SignupRequest signupRequest) {

        // Input verification
        if(signupRequest == null) {
            log.error("[" + new Date() + "] => EMAIL NULL OR EMPTY >>>>>>>> askRegistrationCode :: AuthController.java");
            return ResponseEntity.badRequest().body(null);
        }

        if (userService.existsByUsername(signupRequest.getEmail())) {
            log.error("[" + new Date() + "] => Error: Username is already taken! >>>>>>>> askRegistrationCode :: AuthController.java");
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        if (userService.existsByEmail(signupRequest.getEmail())){
            log.error("[" + new Date() + "] => Error: Email is already in use! >>>>>>>> askRegistrationCode :: AuthController.java");
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        HashMap<String, String> emailValidity = fieldValidation.isEmailValid(signupRequest.getEmail());
        if(!Boolean.parseBoolean(emailValidity.get("isEmailValid"))) {
            log.error("[" + new Date() + "] => Email is invalid! (" + emailValidity.get("message") + ") >>>>>>>> askRegistrationCode :: AuthController.java");
            return ResponseEntity
                    .badRequest()
                    .body(emailValidity.get("message"));
        }

        HashMap<String, String> passValidity = fieldValidation.isPasswordValid(signupRequest.getPassword());
        if(!Boolean.parseBoolean(passValidity.get("isPasswordValid"))) {
            log.error("[" + new Date() + "] => Password is invalid! (" + passValidity.get("message") + ") >>>>>>>> askRegistrationCode :: AuthController.java");
            return ResponseEntity
                    .badRequest()
                    .body(passValidity.get("message"));
        }

        // Here we create the verification code
        VerificationCode verificationCode = verificationCodeService.createSignupCode(signupRequest.getEmail());

        // Once the code is created, we send it to the user via his email address
        Email emailMessage = new Email();
        emailMessage.setMessage(Integer.toString(verificationCode.getCode()));
        List<String> toAddresses = new ArrayList<>();
        toAddresses.add(signupRequest.getEmail());
        emailMessage.setTo(toAddresses);

        emailService.sendSignupConfirmationEmail(emailMessage);

        return ResponseEntity.ok(true);

    }
}
