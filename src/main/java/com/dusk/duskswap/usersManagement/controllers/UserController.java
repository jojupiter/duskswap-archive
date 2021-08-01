package com.dusk.duskswap.usersManagement.controllers;

import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.mailing.models.Email;
import com.dusk.duskswap.commons.mailing.services.EmailService;
import com.dusk.duskswap.commons.models.VerificationCode;
import com.dusk.duskswap.commons.services.UtilitiesService;
import com.dusk.duskswap.commons.services.VerificationCodeService;
import com.dusk.duskswap.usersManagement.entityDto.PasswordUpdateDto;
import com.dusk.duskswap.usersManagement.models.Enterprise;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UtilitiesService utilitiesService;
    @Autowired
    private JwtUtils jwtUtils;

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    //================== USER MANAGEMENT =======================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/update/{id}", produces = "application/json")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @Valid @RequestBody User newUser) {
        return userService.updateUser(id, newUser);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping(value = "/update", produces = "application/json")
    public ResponseEntity<User> updateUser(@RequestBody User newUser, @RequestParam(name = "oldPassword") String oldPassword) {
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> updateUser :: UserController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return userService.updateUser(user.get(), newUser, oldPassword);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping(value = "/check-email", produces = "application/json")
    public ResponseEntity<Boolean> checkEmail(@RequestParam(name = "email") String email) {
        // input checking
        if(email == null) {
            logger.error("[" + new Date() + "] => INPUT NULL (email) >>>>>>>> checkEmail :: UserController.java");
            return ResponseEntity.badRequest().body(false);
        }

        // >>>>> 1. we check if the email is possessed by a user or not
        Boolean isEmailExist = userService.existsByEmail(email);
        if(!isEmailExist) {
            logger.error("[" + new Date() + "] => EMAIL DOESN'T EXIST >>>>>>>> checkEmail :: UserController.java");
            return ResponseEntity.ok(false);
        }

        // >>>>> 2. if the email exists, we create a verification code and we send it to its email
        VerificationCode verificationCode = verificationCodeService.createForgotPasswordCode(email);
        if(verificationCode == null) {
            logger.error("[" + new Date() + "] => VERIFICATION CODE NULL >>>>>>>> checkEmail :: UserController.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Email emailMessage = new Email();
        emailMessage.setMessage(Integer.toString(verificationCode.getCode()));
        List<String> toAddresses = new ArrayList<>();
        toAddresses.add(email);
        emailMessage.setTo(toAddresses);
        emailService.sendForgotPasswordEmail(emailMessage);

        return ResponseEntity.ok(true);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping(value = "/update-password", produces = "application/json")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateDto dto) {
        // input checking
        if(
                dto == null ||
                (dto != null &&
                        (
                                dto.getCode() == null || (dto.getCode() != null && dto.getCode().isEmpty()) ||
                                dto.getEmail() == null || (dto.getEmail() != null && dto.getEmail().isEmpty()) ||
                                dto.getNewPassword() == null || (dto.getNewPassword() != null && dto.getNewPassword().isEmpty())
                        )
                )
        ) {
            logger.error("[" + new Date() + "] => INPUT NULL (dto = " + dto + ") >>>>>>>> updateString :: UserController.java");
            return ResponseEntity.badRequest().body(null);
        }

        // >>>>> 1. getting the user
        Optional<User> user = userService.getUserByEmail(dto.getEmail());
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => EMAIL DOESN'T EXIST >>>>>>>> checkEmail :: UserController.java");
            return new ResponseEntity(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 2. updating user password
        User user1 = userService.updateUserPassword(user.get(), dto.getNewPassword());
        if(user1 == null) {
            logger.error("[" + new Date() + "] => EMAIL DOESN'T EXIST >>>>>>>> checkEmail :: UserController.java");
            return new ResponseEntity(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok("Updated Successfully!");
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping(value = "/suspend", produces = "application/json")
    public ResponseEntity<User> suspendUser() {
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> suspendUser :: UserController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return userService.suspendUser(user.get());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/suspend-admin", produces = "application/json")
    public ResponseEntity<User> adminSuspendUser(@RequestParam(name = "id") Long id) {
        return userService.adminSuspendUser(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/activate", produces = "application/json")
    public ResponseEntity<User> activateUser(@RequestParam(name = "id") Long id) {
        return userService.activateUser(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/disable", produces = "application/json")
    public ResponseEntity<User> disableUser(@RequestParam(name = "id") Long id) {
        return userService.disableUser(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<User> getUser(@PathVariable("id") Long id)
    {
        return userService.getUserById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/username", produces = "application/json")
    public User getUserByUsername(@RequestParam(name = "username") String username)
    {
        return userService.getUserByUsername(username).get();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<List<User>> getAllUser()
    {
        return userService.getAllUsers();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping(value = "/token", produces = "application/json")
    public User getUser(@RequestParam(name = "token") String token) {
        String email = jwtUtils.getEmailFromJwtToken(token);
        return userService.getUserByEmail(email).get();
    }

    //================= ENTERPRISE MANAGEMENT =================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/enterprises/update", produces = "application/json")
    public ResponseEntity<Enterprise> updateEnterprise(@RequestParam(name = "id") Long id, @Valid @RequestBody Enterprise newEnterprise) {
        return userService.updateEnterprise(id, newEnterprise);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/enterprises/update/token")
    public ResponseEntity<Enterprise> updateEnterprise(@Valid @RequestBody Enterprise newEnterprise) {
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> updateEnterprise :: UserController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return userService.updateEnterprise(user.get(), newEnterprise);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/enterprises/{id}", produces = "application/json")
    public ResponseEntity<Enterprise> getEnterpriseById(@PathVariable("id") Long id) {
        return userService.getEnterpriseById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/enterprises",produces = "application/json")
    public ResponseEntity<Enterprise> getEnterpriseByOwner(@RequestParam(name = "owner_id") Long owner_id) {
        return userService.getEnterpriseByOwner(owner_id);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping(value = "/enterprises/token", produces = "application/json")
    public ResponseEntity<Enterprise> getEnterpriseByOwner() {
        Optional<User> user = utilitiesService.getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getEnterpriseByOwner :: UserController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return userService.getEnterpriseByOwner(user.get());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/enterprises/all", produces = "application/json")
    public ResponseEntity<List<Enterprise>> getAllEnterprises() {
        return userService.getAllEnterprise();
    }

}
