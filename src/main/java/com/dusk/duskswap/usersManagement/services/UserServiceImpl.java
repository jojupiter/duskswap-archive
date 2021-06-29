package com.dusk.duskswap.usersManagement.services;

import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.Status;
import com.dusk.duskswap.commons.repositories.StatusRepository;
import com.dusk.duskswap.usersManagement.models.Enterprise;
import com.dusk.duskswap.usersManagement.models.Role;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.repositories.EnterpriseRepository;
import com.dusk.duskswap.usersManagement.repositories.RoleRepository;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private EnterpriseRepository enterpriseRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private PasswordEncoder encoder;

    //======================= ENTERPRISE SERVICES ================================
    @Override
    public ResponseEntity<Enterprise> getEnterpriseById(Long id) {
        if(id != null)
            return ResponseEntity.ok(enterpriseRepository.findById(id).get());
        return ResponseEntity.badRequest().body(null);
    }

    @Override
    public ResponseEntity<Enterprise> getEntrepriseByOwner(Long owner_id) {
        if(owner_id != null)
            return ResponseEntity.ok(enterpriseRepository.findByOwnerId(owner_id).get());
        return ResponseEntity.badRequest().body(null);
    }

    @Override
    public ResponseEntity<Enterprise> getEnterpriseByOwnerEmail(String email) {
        if(email.isEmpty() || email == null)
            return ResponseEntity.badRequest().body(null);
        Optional<User> owner = userRepository.findByEmail(email);
        if(owner.isPresent()) {
            return getEntrepriseByOwner(owner.get().getId());
        }
        return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Override
    public ResponseEntity<List<Enterprise>> getAllEnterprise() {
        return ResponseEntity.ok(enterpriseRepository.findAll());
    }
    @Override
    public ResponseEntity<Enterprise> createEnterprise(Enterprise enterprise) {
        if(enterprise == null)
            return ResponseEntity.badRequest().body(null);
        return ResponseEntity.ok(enterpriseRepository.save(enterprise));
    }
    @Override
    public ResponseEntity<Enterprise> updateEnterprise(Long id, Enterprise newEnterprise){
        if(id == null || newEnterprise == null)
            return ResponseEntity.badRequest().body(null);
        newEnterprise.setId(id);
        return ResponseEntity.ok(enterpriseRepository.save(newEnterprise));
    }

    @Override
    public ResponseEntity<Enterprise> updateEnterprise(String email, Enterprise newEnterprise) {
        if(email.isEmpty() || email == null || newEnterprise == null)
            return ResponseEntity.badRequest().body(null);
        Enterprise enterprise = getEnterpriseByOwnerEmail(email).getBody();
        if(enterprise != null) {
            return updateEnterprise(enterprise.getId(), newEnterprise);
        }
        return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Override
    public void deleteEnterprise(Long id) {
        if(id != null)
            enterpriseRepository.deleteById(id);
    }

    //======================== USER SERVICES =====================================
    @Override
    public ResponseEntity<User> getUserById(Long id) {
        if(id != null)
            return ResponseEntity.ok(userRepository.findById(id).get());
        return null;
    }
    @Override
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
    @Override
    public User addUser(User user) {
        if(user == null)
            return null;
        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_USER_ACTIVATED);
        if(status.isPresent())
            user.setStatus(status.get());
        return userRepository.save(user);
    }
    @Override
    public ResponseEntity<User> updateUser(Long id, User newUser){
        if(id == null || newUser == null)
            return ResponseEntity.badRequest().body(null);
        newUser.setId(id);
        // check the case if the new user doesn't supply a password
        if(newUser.getEncryptedPassword().isEmpty() || newUser.getEncryptedPassword() == null)
        {
            Optional<User> user = userRepository.findById(id);
            if(user.isPresent())
                newUser.setEncryptedPassword(user.get().getEncryptedPassword());
        }
        else
            newUser.setEncryptedPassword(encoder.encode(newUser.getEncryptedPassword()));
        return ResponseEntity.ok(userRepository.save(newUser));
    }

    @Override
    public ResponseEntity<User> updateUser(String email, User newUser) {
        if(
                email == null || (email != null && email.isEmpty())
                || newUser == null
        )
            return ResponseEntity.badRequest().body(null);
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isPresent()){
            return updateUser(user.get().getId(), newUser);
        }
        return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Override
    public ResponseEntity<User> suspendUser(String email) {
        if(email == null || (email != null && email.isEmpty()))
            return ResponseEntity.badRequest().body(null);
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isPresent()) {
            Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_USER_SELF_SUSPENDED);
            if(status.isPresent()) {
                user.get().setStatus(status.get());
                userRepository.save(user.get());
                return ResponseEntity.ok(user.get());
            }
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Override
    public ResponseEntity<User> adminSuspendUser(Long id) {
        if(id == null)
            return ResponseEntity.badRequest().body(null);
        return changeUserStatus(id, DefaultProperties.STATUS_USER_SUSPENDED_BY_SUPERADMIN);
    }

    @Override
    public ResponseEntity<User> activateUser(Long id) {
        if(id == null)
            return ResponseEntity.badRequest().body(null);
        return changeUserStatus(id, DefaultProperties.STATUS_USER_ACTIVATED);
    }

    @Override
    public ResponseEntity<User> disableUser(Long id) {
        if(id == null)
            return ResponseEntity.badRequest().body(null);
        return changeUserStatus(id, DefaultProperties.STATUS_USER_DISABLED);
    }

    private ResponseEntity<User> changeUserStatus(Long id, String newStatus) {
        if(
                id == null ||
                newStatus == null || (newStatus != null && newStatus.isEmpty())
        )
            return  ResponseEntity.badRequest().body(null);
        Optional<User> user = userRepository.findById(id);
        if(!user.isPresent())
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);

        if(user.get().getStatus() != null) {
            Optional<Status> status = statusRepository.findById(user.get().getStatus().getId());
            if(status.isPresent()) {
                if(     status.get().getName().equals(DefaultProperties.STATUS_USER_SUSPENDED_BY_SUPERADMIN) ||
                        status.get().getName().equals(DefaultProperties.STATUS_USER_SELF_SUSPENDED)
                )
                    return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        user.get().setStatus(statusRepository.findByName(newStatus).get());
        userRepository.save(user.get());

        return ResponseEntity.ok(user.get());
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        if(username == null || (username != null && username.isEmpty()))
            return Optional.empty();
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        if(email == null || (email != null && email.isEmpty()))
            return Optional.empty();
        return userRepository.findByEmail(email);
    }

    @Override
    public Boolean existsByUsername(String username) {
        if(username == null || (username != null && username.isEmpty()))
            return false;
        return userRepository.existsByUsername(username);
    }

    @Override
    public Boolean existsByEmail(String email) {
        if(email == null || (email != null && email.isEmpty()))
            return false;
        return userRepository.existsByEmail(email);
    }

    //================================ ROLE SERVICES ========================================
    @Override
    public Optional<Role> getRoleByName(String name) {
        if(name == null || (name != null && name.isEmpty()))
            return Optional.empty();
        return roleRepository.findByName(name);
    }
    @Override
    public ResponseEntity<List<Role>> getAllRole() {
        return ResponseEntity.ok(roleRepository.findAll());
    }
    @Override
    public ResponseEntity<Role> updateRole(Long id, Role role) {
        if(id == null || role == null)
            return ResponseEntity.badRequest().body(null);

        role.setId(id);
        return ResponseEntity.ok(roleRepository.save(role));
    }

}
