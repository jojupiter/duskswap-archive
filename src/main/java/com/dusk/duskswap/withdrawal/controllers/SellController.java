package com.dusk.duskswap.withdrawal.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.administration.models.DefaultConfig;
import com.dusk.duskswap.administration.services.DefaultConfigService;
import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.mailing.models.Email;
import com.dusk.duskswap.commons.mailing.services.EmailService;
import com.dusk.duskswap.commons.miscellaneous.CodeErrors;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.VerificationCode;
import com.dusk.duskswap.commons.services.VerificationCodeService;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.services.UserService;
import com.dusk.duskswap.withdrawal.entityDto.SellDto;
import com.dusk.duskswap.withdrawal.entityDto.SellPage;
import com.dusk.duskswap.withdrawal.models.Sell;
import com.dusk.duskswap.withdrawal.services.SellService;
import com.dusk.externalAPIs.apiInterfaces.interfaces.MobileMoneyOperations;
import com.dusk.externalAPIs.apiInterfaces.models.AuthRequest;
import com.dusk.externalAPIs.apiInterfaces.models.AuthResponse;
import com.dusk.externalAPIs.apiInterfaces.models.MobileMoneyTransferInfo;
import com.dusk.externalAPIs.apiInterfaces.models.MobileMoneyTransferResponse;
import com.dusk.externalAPIs.cinetpay.constants.CinetpayParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/sell")
@Slf4j
public class SellController {

    @Autowired
    private SellService sellService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private MobileMoneyOperations mobileMoneyOperations;
    @Autowired
    private DefaultConfigService defaultConfigService;
    @Autowired
    private JwtUtils jwtUtils;

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/confirm", produces = "application/json")
    public ResponseEntity<?> confirmation(@RequestBody SellDto sellDto) throws Exception{
        // input checking
        if(
            sellDto == null
        ) {
            log.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> confirmation :: SellController.java");
            return ResponseEntity.badRequest().body(null);
        }
        // >>>>> 1. getting the current authenticated user
        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 2. we check the account's balance
        ExchangeAccount account = accountService.getAccountByUser(user.get());
        if(account == null) {
            log.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT FOUND >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(CodeErrors.EXCHANGE_ACCOUNT_NOT_EXIST, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if(!accountService.isBalanceSufficient(account, sellDto.getFromCurrencyId(), sellDto.getAmount())) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(CodeErrors.INSUFFICIENT_BALANCE_USER, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. here we verify if the provided code is correct
        if(!verificationCodeService.isCodeCorrect(user.get().getEmail(), sellDto.getCode(), DefaultProperties.VERIFICATION_WITHDRAWAL_SELL_PURPOSE)) {
            log.error("[" + new Date() + "] => CODE PROVIDED NOT CORRECT >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(CodeErrors.VERIFICATION_CODE_INCORRECT, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 4. next we update the verification code in order the user won't send the same request twice (this is to avoid issues like debiting multiple time an account for a single operation)
        verificationCodeService.updateCode(user.get().getEmail(), DefaultProperties.VERIFICATION_WITHDRAWAL_SELL_PURPOSE);

        // >>>>> 5. getting the usd-xaf exchange rate
        String usdXafRate = "";
        DefaultConfig config = defaultConfigService.getConfigs();
        if(config == null) {
            usdXafRate = DefaultProperties.DEFAULT_USD_XAF_BUY_RATE;
        }
        else
            usdXafRate = config.getUsdToXafBuy();

        // >>>>> 6. then we create the sale
        Sell sell = sellService.createSell(sellDto, usdXafRate, user.get(), account);
        if(sell == null) {
            log.error("[" + new Date() + "] => THE SELL OBJECT WASN'T CREATED >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // ================================== CALLING MOBILE MONEY TRANSFER METHODS =============================================================
        AuthRequest authRequest = new AuthRequest();
        AuthResponse authResponse = mobileMoneyOperations.authenticate(authRequest);
        if(authResponse == null) {
            log.error("[" + new Date() + "] => TRANSFER AUTHENTICATION FAILED >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Double transferBalance = mobileMoneyOperations.getTransferBalance(authResponse.getToken(), "fr");
        if(transferBalance == null) {
            log.error("[" + new Date() + "] => BALANCE NULL >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if(transferBalance <= Double.parseDouble(sell.getAmountReceived())) {
            log.error("[" + new Date() + "] => INSUFFICIENT BALANCE >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        MobileMoneyTransferInfo info = new MobileMoneyTransferInfo();
        info.setEmail(user.get().getEmail());
        info.setPhone(sellDto.getTel());
        info.setAmount(sell.getAmountReceived());
        info.setFirstName(user.get().getFirstName());
        info.setLastName(user.get().getLastName());

        MobileMoneyTransferResponse response = mobileMoneyOperations.performTransfer(authResponse.getToken(), "fr", info);
        if(response == null) {
            log.error("[" + new Date() + "] => TRANSFER FAILED >>>>>>>> confirmation :: SellController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // ==================================================================================================================================
        sellService.saveSell(sell);

        return ResponseEntity.ok(true);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/ask-code", produces = "application/json")
    public ResponseEntity<Boolean> askCode() {
        // first we get the current authenticated user
        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> askCode :: SellController.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        VerificationCode code = verificationCodeService.createWithdrawalCode(user.get().getEmail());
        if(code == null) {
            log.error("[" + new Date() + "] => CODE NULL >>>>>>>> askCode :: SellController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Email email = new Email();
        email.setMessage(Integer.toString(code.getCode()));
        List<String> toAddresses = new ArrayList<>();
        toAddresses.add(user.get().getEmail());
        email.setTo(toAddresses);

        emailService.sendWithdrawalEmail(email);

        return ResponseEntity.ok(true);
    }

    @PostMapping(value = "/check-status", produces = "application/json")
    public void updateSell(@RequestBody Map<String, Object> notifyBody) throws Exception {

        if(!notifyBody.containsKey("client_transaction_id")) {
            log.error("[" + new Date() + "] =>  REQUEST BODY DOESN'T CONTAIN client_transaction_id key >>>>>>>> updateSell :: SellController.java");
            return;
        }

        Optional<Sell> sell = sellService.getSellByTransactionId(notifyBody.get("client_transaction_id").toString());
        if(!sell.isPresent()) {
            log.error("[" + new Date() + "] =>  CORRESPONDING SELL DOESN'T EXIST >>>>>>>> updateSell :: SellController.java");
            return;
        }

        if(
                sell.get().getStatus().equals(DefaultProperties.STATUS_TRANSACTION_CONFIRMED) ||
                sell.get().getStatus().equals(DefaultProperties.STATUS_TRANSACTION_INVALID)
        ){
            log.info("[" + new Date() + "] =>  SELL STATUS ALREADY CONFIRMED OR INVALID >>>>>>>> updateSell :: SellController.java");
            return;
        }
        AuthRequest authRequest = new AuthRequest();
        AuthResponse authResponse = mobileMoneyOperations.authenticate(authRequest);
        if(authResponse == null) {
            log.error("[" + new Date() + "] =>  CINETPAY AUTHENTICATION FAILED >>>>>>>> updateSell :: SellController.java");
            return;
        }

        MobileMoneyTransferInfo transferInfo = mobileMoneyOperations.getTransferInformation(authResponse.getToken(), sell.get().getTransactionId(), "fr");
        if(transferInfo == null) {
            log.error("[" + new Date() + "] =>  TRANSFER INFO NULL >>>>>>>> updateSell :: SellController.java");
            return;
        }

        if(transferInfo.getStatus().equals(CinetpayParams.STATUS_TRANSFER_TREATMENT_VAL)) {
            sellService.updateSellStatus(sell.get(), DefaultProperties.STATUS_TRANSACTION_CONFIRMED);
            accountService.debitAccount(sell.get().getExchangeAccount(),
                    sell.get().getCurrency(),
                    sell.get().getTotalAmountCrypto());
            return;
        }
        if(transferInfo.getStatus().equals(CinetpayParams.STATUS_TRANSFER_TREATMENT_REJ)) {
            sellService.updateSellStatus(sell.get(), DefaultProperties.STATUS_TRANSACTION_INVALID);
            return;
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/user-all", produces = "application/json")
    public ResponseEntity<?> getAllUserSales(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserSales :: SellController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return sellService.getAllSell(user.get(), currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/user-all", produces = "application/json", params = "userId")
    public ResponseEntity<?> getAllUserSales(@RequestParam(name = "userId") Long userId,
                                             @RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        Optional<User> user = userService.getUser(userId);
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> etAllUserSales :: SellController.java");
            return new ResponseEntity<>(CodeErrors.USER_NOT_PRESENT, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return sellService.getAllSell(user.get(), currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<SellPage> getAllSales(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return sellService.getAllSell(currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all-profits", produces = "application/json")
    public ResponseEntity<String> getAllSaleProfits() {
        return sellService.getAllSellProfits();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all-profits-before", produces = "application/json")
    public ResponseEntity<String> getAllSaleProfitsBefore(@RequestParam(name = "date") @DateTimeFormat(pattern = "dd/MM/yyyy HH:ss") Date date) {
        return sellService.getAllSellProfitsBefore(date);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all-profits-after", produces = "application/json")
    public ResponseEntity<String> getAllSaleProfitsAfter(@RequestParam(name = "date") @DateTimeFormat(pattern = "dd/MM/yyyy HH:ss") Date date) {
        return sellService.getAllSellProfitsAfter(date);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all-profits-between", produces = "application/json")
    public ResponseEntity<String> getAllSaleProfitsBetween(@RequestParam(name = "startDate") @DateTimeFormat(pattern = "dd/MM/yyyy HH:ss") Date startDate,
                                                           @RequestParam(name = "endDate") @DateTimeFormat(pattern = "dd/MM/yyyy HH:ss") Date endDate) {
        return sellService.getAllSellProfitsBetween(startDate, endDate);
    }

}
