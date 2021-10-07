package com.dusk.duskswap.deposit.controllers;

import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.commons.miscellaneous.CodeErrors;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.miscellaneous.Utilities;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.services.UtilitiesService;
import com.dusk.duskswap.deposit.entityDto.BuyDto;
import com.dusk.duskswap.deposit.entityDto.BuyPage;
import com.dusk.duskswap.deposit.models.Buy;
import com.dusk.duskswap.deposit.services.BuyService;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.services.UserService;
import com.dusk.externalAPIs.apiInterfaces.interfaces.MobileMoneyOperations;
import com.dusk.externalAPIs.apiInterfaces.models.MobileMoneyPaymentRequest;
import com.dusk.externalAPIs.apiInterfaces.models.MobileMoneyPaymentResponse;
import com.dusk.externalAPIs.cinetpay.models.VerificationResponse;
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
    @Autowired
    private MobileMoneyOperations mobileMoneyOperations;
    @Autowired
    private UtilitiesService utilitiesService;

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
    public ResponseEntity<?> buyRequest(BuyDto dto) {
        // input checking
        if(
                dto == null ||
                (
                    dto != null &&
                            (
                                dto.getAmount() == null || (dto.getAmount() != null && dto.getAmount().isEmpty()) || (dto.getAmount() != null && !dto.getAmount().isEmpty() && Double.parseDouble(dto.getAmount()) <= 0) ||
                                dto.getTransactionOptId() == null ||
                                dto.getToCurrencyId() == null
                            )
                )
        ) {
            log.error("[" + new Date() + "] => INPUT ERROR >>>>>>>> buyRequest :: BuyController.java " +
                    " ===== dto = " + dto);
            return ResponseEntity.badRequest().body(null);
        }

        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> buyRequest :: BuyController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<TransactionOption> transactionOption = utilitiesService.getTransactionOption(dto.getTransactionOptId());
        if(!transactionOption.isPresent()) {
            log.error("[" + new Date() + "] => TRANSACTION OPTION NOT PRESENT >>>>>>>> buyRequest :: BuyController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<Currency> currency = utilitiesService.getCurrencyById(dto.getToCurrencyId());
        if(!currency.isPresent()) {
            log.error("[" + new Date() + "] => CRYPTO CURRENCY NOT PRESENT >>>>>>>> buyRequest :: BuyController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        MobileMoneyPaymentRequest request = new MobileMoneyPaymentRequest();
        request.setAmount(dto.getAmount());
        request.setCustomerId(Long.toString(user.get().getId()));
        if(user.get().getFirstName() == null || (user.get().getFirstName() != null && user.get().getFirstName().isEmpty()))
            request.setCustomerFirstName(user.get().getEmail());
        else
            request.setCustomerFirstName(user.get().getFirstName());
        request.setCustomerLastName(user.get().getLastName());
        String txId = "";
        do {
            txId = Utilities.generateUUID();
        }
        while (buyService.existsByTxId(txId));
        request.setTransactionId(txId);
        request.setMetadata("User" + user.get().getId());

        // Generation of the payment URL
        MobileMoneyPaymentResponse response = mobileMoneyOperations.performPayment(request);
        if(response == null) {

        }

        //Buy buy = buyService.createBuy();
        return null;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping(value = "/check-status", produces = "application/json")
    public void checkStatus(@RequestParam(name = "cpm_trans_id") String transactionId,
                            @RequestParam(name = "cpm_site_id") String siteId) throws Exception { // check status for cinetpay
        // input checking
        if(transactionId == null)
            return;

        // we get the corresponding buy object
        Optional<Buy> buy = buyService.getByTransactionId(transactionId);
        if(!buy.isPresent()) {
            log.error("[" + new Date() + "] => CANNOT FIND CORRESPONDING BUY >>>>>>>> checkStatus :: BuyController.java");
            return;
        }

        // then we check if the status is already "confirmed"
        if(buy.get().getStatus().getName().equals(DefaultProperties.STATUS_TRANSACTION_CONFIRMED)) {
            log.info("[" + new Date() + "] => BUY ALREADY CONFIRMED >>>>>>>> checkStatus :: BuyController.java");
            return;
        }

        VerificationResponse verificationResponse = mobileMoneyOperations.checkPaymentStatus(buy.get().getPayToken());
        if(verificationResponse == null) {
            log.error("[" + new Date() + "] => VERIFICATION RESPONSE NULL >>>>>>>> checkStatus :: BuyController.java");
            return;
        }

        Buy savedBuy =  buyService.confirmBuy(buy.get());
        log.info("[" + new Date() + "] => CONFIRMED BUY : " + savedBuy + " >>>>>>>> checkStatus :: BuyController.java");

        return;
    }

}
