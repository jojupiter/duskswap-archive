package com.dusk.duskswap.usersManagement.services;

import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.Level;
import com.dusk.duskswap.commons.models.Status;
import com.dusk.duskswap.commons.repositories.LevelRepository;
import com.dusk.duskswap.commons.repositories.StatusRepository;
import com.dusk.duskswap.usersManagement.controllers.AuthController;
import com.dusk.duskswap.usersManagement.models.Enterprise;
import com.dusk.duskswap.usersManagement.models.Role;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.repositories.EnterpriseRepository;
import com.dusk.duskswap.usersManagement.repositories.RoleRepository;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    private LevelRepository levelRepository;
    @Autowired
    private PasswordEncoder encoder;
    private Logger logger = LoggerFactory.getLogger(AuthController.class);

    //======================= ENTERPRISE SERVICES ================================
    @Override
    public ResponseEntity<Enterprise> getEnterpriseById(Long id) {
        // input checking
        if(id == null) {
            logger.error("[" + new Date() + "] => ENTREPRISE ID NULL >>>>>>>> getEnterpriseById :: UserServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(enterpriseRepository.findById(id).get());
    }

    @Override
    public ResponseEntity<Enterprise> getEnterpriseByOwner(Long owner_id) {
        // input checking
        if(owner_id == null) {
            logger.error("[" + new Date() + "] => ENTERPRISE OWNER ID NULL >>>>>>>> getEntrepriseByOwner :: UserServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(enterpriseRepository.findByOwnerId(owner_id).get());
    }

    @Override
    public ResponseEntity<Enterprise> getEnterpriseByOwner(User user) {
        // input checking
        if(user == null) {
            logger.error("[" + new Date() + "] => ENTERPRISE OWNER NULL(user) >>>>>>>> getEntrepriseByOwner :: UserServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        return getEnterpriseByOwner(user.getId());
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
    public ResponseEntity<Enterprise> updateEnterprise(User user, Enterprise newEnterprise) {
        if(user == null || newEnterprise == null) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> updateEnterprise :: UserServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }
        Optional<Enterprise> enterprise = enterpriseRepository.findByOwnerId(user.getId());
        if(!enterprise.isPresent()) {
            logger.error("[" + new Date() + "] => ENTERPRISE NOT PRESENT >>>>>>>> updateEnterprise :: UserServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return updateEnterprise(enterprise.get().getId(), newEnterprise);
    }

    @Override
    public void deleteEnterprise(Long id) {
        if(id != null)
            enterpriseRepository.deleteById(id);
    }

    //======================== USER SERVICES =====================================
    @Override
    public ResponseEntity<User> getUserById(Long id) {
        if(id == null) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getUserById :: UserServiceImpl.java");
            return null;
        }
        return ResponseEntity.ok(userRepository.findById(id).get());
    }
    @Override
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
    @Override
    public User addUser(User user) {
        // input checking
        if(
                user == null ||
                (user != null &&
                        (user.getEmail() == null || (user.getEmail() != null && user.getEmail().isEmpty()))
                )
        ) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> addUser :: UserServiceImpl.java");
            return null;
        }
        // >>>>> 1. we set the user's "activated" status
        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_USER_ACTIVATED);
        if(!status.isPresent()) {
            logger.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> addUser :: UserServiceImpl.java");
            return null;
        }
        user.setStatus(status.get());
        // >>>>> 2. here we set the level 0 to the user
        Optional<Level> level = levelRepository.findByIso(DefaultProperties.LEVEL_ISO_0);
        if(!level.isPresent()) {
            logger.error("[" + new Date() + "] => LEVEL NOT PRESENT >>>>>>>> addUser :: UserServiceImpl.java");
            return null;
        }
        user.setLevel(level.get());
        // >>>>> 3. we then return the created user
        return userRepository.save(user);
    }
    @Override
    public ResponseEntity<User> updateUser(Long id, User newUser) {
        // inputs checking
        if(id == null || newUser == null) {
            logger.error("[" + new Date() + "] => LEVEL NOT PRESENT >>>>>>>> updateUser :: UserServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        // >>>>> 1. we get the corresponding user
        Optional<User> user = userRepository.findById(id);
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => LEVEL NOT PRESENT >>>>>>>> updateUser :: UserServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>> 2. we check if email was modify / left empty or null, in that case an error should be raised, because we need a special method for that
        if(
                newUser.getEmail() == null || (newUser.getEmail() != null && newUser.getEmail().isEmpty()) ||
                (newUser.getEmail() != null && !newUser.getEmail().equals(user.get().getEmail()))
        ) {
            logger.error("[" + new Date() + "] => NEW EMAIL IS NULL/EMPTY (email=" + newUser.getEmail()+ ") >>>>>>>> updateUser :: UserServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>> 3. we check the case where the new user supplies a new password
        if(newUser.getEncryptedPassword() != null)
            user.get().setEncryptedPassword(newUser.getEncryptedPassword());

        // >>>>> 4. we save the user
        return ResponseEntity.ok(userRepository.save(user.get()));
    }

    @Override
    public ResponseEntity<User> updateUser(User user, User newUser, String oldPassword) {
        // inputs checking
        if(user == null || newUser == null) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> updateUser :: UserServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        // >>>>> 1. we check if email was modify / left empty or null, in that case an error should be raised, because we need a special method for that
        if(newUser.getEmail() != null && !newUser.getEmail().equals(user.getEmail()))
        {// if new user's email is different from previous one
            logger.error("[" + new Date() + "] => NEW EMAIL IS NULL/EMPTY (email=" + newUser.getEmail()+ ") >>>>>>>> updateUser :: UserServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>> 2. we then verify if the old password is correct
        if(!encoder.encode(oldPassword).equals(user.getEncryptedPassword())) {
            logger.error("[" + new Date() + "] => PROVIDED OLD PASSWORD INCORRECT >>>>>>>> updateUser :: UserServiceImpl.java" +
                    " === old = " + encoder.encode(oldPassword) + " user pas = " + user.getEncryptedPassword());
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>> 3. we check the case where the new user supplies a new password
        if(newUser.getEncryptedPassword() != null)
            user.setEncryptedPassword(newUser.getEncryptedPassword());

        // >>>>> 4. we save the user
        return ResponseEntity.ok(userRepository.save(user));
    }

    @Override
    public User updateUserPassword(User user, String newPassword) {
        // input checking
        if(
                user == null ||
                newPassword == null || (newPassword != null && newPassword.isEmpty())
        ) {
            logger.error("[" + new Date() + "] => EMAIL NULL OR EMPTY >>>>>>>> suspendUser :: UserServiceImpl.java");
            return null;
        }

        // updating user's password
        user.setEncryptedPassword(encoder.encode(newPassword));

        return userRepository.save(user);
    }

    @Override
    public ResponseEntity<User> suspendUser(User user) {
        // input checking
        if(user == null) {
            logger.error("[" + new Date() + "] => EMAIL NULL OR EMPTY >>>>>>>> suspendUser :: UserServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }
        // >>>>> 1. we find the user self suspended status
        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_USER_SELF_SUSPENDED);
        if(status.isPresent()) {
            logger.error("[" + new Date() + "] => STATUS (Self suspended) NOT PRESENT >>>>>>>> suspendUser :: UserServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>> 2. we set the status to that user
        user.setStatus(status.get());
        // >>>>> 3. we save the user
        return ResponseEntity.ok(userRepository.save(user));
    }

    @Override
    public ResponseEntity<User> adminSuspendUser(Long id) {
        // input checking
        if(id == null) {
            logger.error("[" + new Date() + "] => USER ID NULL OR EMPTY >>>>>>>> suspendUser :: UserServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }
        // here we call the generic method for changing user status by assigning STATUS_USER_SUSPENDED_BY_SUPERADMIN to the user
        return changeUserStatus(id, DefaultProperties.STATUS_USER_SUSPENDED_BY_SUPERADMIN);
    }

    @Override
    public ResponseEntity<User> activateUser(Long id) {
        // input checking
        if(id == null) {
            logger.error("[" + new Date() + "] => USER ID NULL OR EMPTY >>>>>>>> activateUse :: UserServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }
        return changeUserStatus(id, DefaultProperties.STATUS_USER_ACTIVATED);
    }

    @Override
    public ResponseEntity<User> disableUser(Long id) {
        if(id == null){
            logger.error("[" + new Date() + "] => USER ID NULL OR EMPTY >>>>>>>> disableUser :: UserServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }
        return changeUserStatus(id, DefaultProperties.STATUS_USER_DISABLED);
    }

    private ResponseEntity<User> changeUserStatus(Long id, String newStatus) {
        // input checking
        if(
                id == null ||
                newStatus == null || (newStatus != null && newStatus.isEmpty())
        ) {
            logger.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> changeUserStatus :: UserServiceImpl.java " +
                    " ====== user_id = " + id + ", newStatus = " + newStatus);
            return  ResponseEntity.badRequest().body(null);
        }
        // >>>>> 1. we get the user
        Optional<User> user = userRepository.findById(id);
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> changeUserStatus :: UserServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>> 3. we check whether or not the user is suspended. If suspended we can't anymore change his status
        if(user.get().getStatus() != null) {
            Optional<Status> status = statusRepository.findById(user.get().getStatus().getId());
            if(status.isPresent()) {
                if(     status.get().getName().equals(DefaultProperties.STATUS_USER_SUSPENDED_BY_SUPERADMIN) ||
                        status.get().getName().equals(DefaultProperties.STATUS_USER_SELF_SUSPENDED)
                ){
                    logger.error("[" + new Date() + "] => USER SUSPENDED, CANNOT CHANGE HIS STATUS >>>>>>>> changeUserStatus :: UserServiceImpl.java");
                    return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
                }
            }
        }
        // >>>>> 4. if the user is not suspended we properly update his status
        user.get().setStatus(statusRepository.findByName(newStatus).get());
        userRepository.save(user.get());

        return ResponseEntity.ok(user.get());
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        // input checking
        if(username == null || (username != null && username.isEmpty())) {
            logger.error("[" + new Date() + "] => INPUT NULL / EMPTY >>>>>>>> getUserByUsername :: UserServiceImpl.java");
            return Optional.empty();
        }
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        // input checking
        if(email == null || (email != null && email.isEmpty())){
            logger.error("[" + new Date() + "] => EMAIL NULL / EMPTY >>>>>>>> getUserByEmail :: UserServiceImpl.java");
            return Optional.empty();
        }
        return userRepository.findByEmail(email);
    }

    @Override
    public Boolean existsByUsername(String username) {
        // input checking
        if(username == null || (username != null && username.isEmpty())) {
            logger.error("[" + new Date() + "] => EMAIL NULL / EMPTY >>>>>>>> existsByUsername :: UserServiceImpl.java");
            return false;
        }
        return userRepository.existsByUsername(username);
    }

    @Override
    public Boolean existsByEmail(String email) {
        // input checking
        if(email == null || (email != null && email.isEmpty())) {
            logger.error("[" + new Date() + "] => EMAIL NULL / EMPTY >>>>>>>> existsByEmail :: UserServiceImpl.java");
            return false;
        }
        return userRepository.existsByEmail(email);
    }

    //================================ ROLE SERVICES ========================================
    @Override
    public Optional<Role> getRoleByName(String name) {
        // input checking
        if(name == null || (name != null && name.isEmpty())) {
            logger.error("[" + new Date() + "] => ROLE NAME NULL / EMPTY >>>>>>>> getRoleByName :: UserServiceImpl.java");
            return Optional.empty();
        }
        return roleRepository.findByName(name);
    }
    @Override
    public ResponseEntity<List<Role>> getAllRole() {
        return ResponseEntity.ok(roleRepository.findAll());
    }
    @Override
    public ResponseEntity<Role> updateRole(Long id, Role role) {
        // input checking
        if(id == null || role == null) {
            logger.error("[" + new Date() + "] => INPUT NULL / EMPTY >>>>>>>> getRoleByName :: UserServiceImpl.java " +
                    "======= id = " + id + ", role = " + role);
            return ResponseEntity.badRequest().body(null);
        }
        role.setId(id);
        return ResponseEntity.ok(roleRepository.save(role));
    }

}
