package com.dusk.duskswap.deposit.controllers;

import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.commons.miscellaneous.CodeErrors;
import com.dusk.duskswap.deposit.entityDto.BuyDto;
import com.dusk.duskswap.deposit.entityDto.BuyPage;
import com.dusk.duskswap.deposit.services.BuyService;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/buy")
@Slf4j
public class BuyController {

    @Autowired
    private BuyService buyService;
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<BuyPage> getAllBuy(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return buyService.getAllBuy(currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/user-all", produces = "application/json")
    public  ResponseEntity<?> getAllUserBuy(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserBuy :: BuyController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return buyService.getAllBuyByUser(user.get(), currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/user-all", produces = "application/json", params = "userId")
    public  ResponseEntity<?> getAllUserBuy(@RequestParam(name = "userId") Long userId,
                                            @RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        Optional<User> user = userService.getUser(userId);
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserBuy :: BuyController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return buyService.getAllBuyByUser(user.get(), currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/buy-request", produces = "application/json")
    public ResponseEntity<String> buyRequest(BuyDto dto) { // this method create a buy command and return notification url
        return null;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping(value = "/update-buy", produces = "application/json")
    public ResponseEntity<String> updateBuy() {
        return null;
    }

}
