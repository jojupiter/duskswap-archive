package com.dusk.shared.usersManagement.controllers;

import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.shared.usersManagement.models.Enterprise;
import com.dusk.shared.usersManagement.models.User;
import com.dusk.shared.usersManagement.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
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
    public ResponseEntity<User> updateUser(@PathVariable("token") String token, @Valid @RequestBody User newUser) {
        String email = jwtUtils.getUserNameFromJwtToken(token);
        return userService.updateUser(email, newUser);
    }


    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping(value = "/suspend", produces = "application/json")
    public ResponseEntity<User> suspendUser(@RequestParam(name = "token") String token) {
        String email = jwtUtils.getUserNameFromJwtToken(token);
        return userService.suspendUser(email);
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
        String email = jwtUtils.getUserNameFromJwtToken(token);
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
    public ResponseEntity<Enterprise> updateEnterprise(@RequestParam(name = "token") String token, @Valid @RequestBody Enterprise newEnterprise) {
        String email = jwtUtils.getUserNameFromJwtToken(token);
        return userService.updateEnterprise(email, newEnterprise);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/enterprises/{id}", produces = "application/json")
    public ResponseEntity<Enterprise> getEnterpriseById(@PathVariable("id") Long id) {
        return userService.getEnterpriseById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/enterprises",produces = "application/json")
    public ResponseEntity<Enterprise> getEnterpriseByOwner(@RequestParam(name = "owner_id") Long owner_id) {
        return userService.getEntrepriseByOwner(owner_id);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping(value = "/enterprises/token", produces = "application/json")
    public ResponseEntity<Enterprise> getEnterpriseByOwner(@RequestParam(name = "token") String token) {
        String email = jwtUtils.getUserNameFromJwtToken(token);
        return userService.getEnterpriseByOwnerEmail(email);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/enterprises/all", produces = "application/json")
    public ResponseEntity<List<Enterprise>> getAllEnterprises() {
        return userService.getAllEnterprise();
    }

}
