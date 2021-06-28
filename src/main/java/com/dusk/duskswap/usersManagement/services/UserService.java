package com.dusk.duskswap.usersManagement.services;

import com.dusk.duskswap.usersManagement.models.*;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

public interface UserService {

    //======================= ENTERPRISE SERVICES ================================
    public ResponseEntity<Enterprise> getEnterpriseById(Long id);
    public ResponseEntity<Enterprise> getEntrepriseByOwner(Long owner_id);
    public ResponseEntity<Enterprise> getEnterpriseByOwnerEmail(String email); // for user
    public ResponseEntity<List<Enterprise>> getAllEnterprise();
    public ResponseEntity<Enterprise> createEnterprise(@Valid Enterprise enterprise);
    public ResponseEntity<Enterprise> updateEnterprise(Long id, @Valid Enterprise newEnterprise);
    public ResponseEntity<Enterprise> updateEnterprise(String email, @Valid Enterprise newEnterprise); // for user
    public void deleteEnterprise(Long id);

    //======================== USER SERVICES =====================================
    public ResponseEntity<User> getUserById(Long id);
    public ResponseEntity<List<User>> getAllUsers();
    public User addUser(@Valid User user);
    public ResponseEntity<User> updateUser(Long id, @Valid User newUser);
    public ResponseEntity<User> updateUser(String email, @Valid User newUser); // for user
    public ResponseEntity<User> suspendUser(String email); // for user: status USER_SELF_SUSPENDED
    public ResponseEntity<User> adminSuspendUser(Long id); // status: USER_SUSPENDED_BY_SUPERADMIN
    public ResponseEntity<User> activateUser(Long id); // status: USER_ACTIVATED
    public ResponseEntity<User> disableUser(Long id); // status: USER_DISABLED
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    //======================= ROLE SERVICES =====================================
    public Optional<Role> getRoleByName(String name);
    public ResponseEntity<List<Role>> getAllRole();
    public ResponseEntity<Role> updateRole(Long id, Role role);

}
