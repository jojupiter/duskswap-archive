package com.dusk.duskswap.usersManagement.services;

import com.dusk.duskswap.usersManagement.models.*;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

public interface UserService {

    //======================= ENTERPRISE SERVICES ================================
    ResponseEntity<Enterprise> getEnterpriseById(Long id);
    ResponseEntity<Enterprise> getEnterpriseByOwner(Long owner_id);
    ResponseEntity<Enterprise> getEnterpriseByOwner(User user); // for user
    ResponseEntity<List<Enterprise>> getAllEnterprise();
    ResponseEntity<Enterprise> createEnterprise(@Valid Enterprise enterprise);
    ResponseEntity<Enterprise> updateEnterprise(Long id, @Valid Enterprise newEnterprise);
    ResponseEntity<Enterprise> updateEnterprise(User user, @Valid Enterprise newEnterprise); // for user
    void deleteEnterprise(Long id);

    //======================== USER SERVICES =====================================
    Optional<User> getCurrentUser();
    ResponseEntity<User> getUserById(Long id);
    Optional<User> getUser(Long id);
    ResponseEntity<List<User>> getAllUsers();
    User addUser(@Valid User user);
    ResponseEntity<User> updateUser(Long id, @Valid User newUser);
    ResponseEntity<User> updateUser(User user, @Valid User newUser, String oldPassword); // for user
    User updateUserPassword(User user, String newPassword);
    ResponseEntity<User> suspendUser(User user); // for user: status USER_SELF_SUSPENDED
    ResponseEntity<User> adminSuspendUser(Long id); // status: USER_SUSPENDED_BY_SUPERADMIN
    ResponseEntity<User> activateUser(Long id); // status: USER_ACTIVATED
    ResponseEntity<User> disableUser(Long id); // status: USER_DISABLED
    ResponseEntity<List<User>> getUserByUsernameOrEmail(String usernameOrEmail);
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    //======================= ROLE SERVICES =====================================
    Optional<Role> getRoleByName(String name);
    ResponseEntity<List<Role>> getAllRoles();
    ResponseEntity<Role> updateRole(Long id, Role role);
    ResponseEntity<?> changeUserRole(Long userId, String roleName);

    // ===================== LEVEL SERVICES ===================================
    ResponseEntity<?> changeLevel(User user, Long levelId);

}
