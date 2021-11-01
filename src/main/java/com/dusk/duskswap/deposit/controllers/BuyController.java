package com.dusk.duskswap.deposit.controllers;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.services.AccountService;
import com.dusk.duskswap.administration.models.DefaultConfig;
import com.dusk.duskswap.administration.models.OverallBalance;
import com.dusk.duskswap.administration.services.DefaultConfigService;
import com.dusk.duskswap.administration.services.OverallBalanceService;
import com.dusk.duskswap.commons.miscellaneous.Codes;
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
import com.dusk.externalAPIs.cinetpay.constants.CinetpayParams;
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
    @Autowired
    private OverallBalanceService overallBalanceService;
    @Autowired
    private DefaultConfigService defaultConfigService;

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
            return new ResponseEntity<>(Codes.USER_NOT_FOUND, HttpStatus.UNPROCESSABLE_ENTITY);
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
            return new ResponseEntity<>(Codes.USER_NOT_FOUND, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return buyService.getAllBuyByUser(user.get(), currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/buy-id", produces = "application/json", params = "userId")
    public  ResponseEntity<?> getBuy(@RequestParam(name = "buyId") Long buyId) {
        return buyService.getBuy(buyId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/buy-request", produces = "application/json")
    public ResponseEntity<?> buyRequest(@RequestBody BuyDto dto) throws Exception {
        // input checking
        if(
                dto == null ||
                (
                    dto != null &&
                            (
                                dto.getAmount() == null || (dto.getAmount() != null && dto.getAmount().isEmpty()) || (dto.getAmount() != null && !dto.getAmount().isEmpty() && Double.parseDouble(dto.getAmount()) <= 0) ||
                                dto.getTransactionOptIso() == null ||
                                dto.getToCurrencyId() == null
                            )
                )
        ) {
            log.error("[" + new Date() + "] => INPUT ERROR >>>>>>>> buyRequest :: BuyController.java " +
                    " ===== dto = " + dto);
            return ResponseEntity.badRequest().body(null);
        }

        // >>>>> 1. we adjust the initial amount
        Double amount = Double.parseDouble(dto.getAmount());
        Double adjustedAmount = Math.floor(amount) - Math.floor(amount) % 5;
        dto.setAmount(Double.toString(adjustedAmount));

        // ========================== Getting the necessary elements =============================
        // >>>>> 1. the current logged in user
        Optional<User> user = userService.getCurrentUser();
        if(!user.isPresent()) {
            log.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> buyRequest :: BuyController.java");
            return new ResponseEntity<>(Codes.USER_NOT_FOUND, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // >>>>> 2. the exchange account of that user
        ExchangeAccount account = accountService.getAccountByUser(user.get());
        if(account == null) {
            log.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> buyRequest :: BuyController.java");
            return new ResponseEntity<>(Codes.EXCHANGE_ACCOUNT_NOT_EXIST, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 2. the crypto currency the user wants to buy
        Optional<Currency> currency = utilitiesService.getCurrencyById(dto.getToCurrencyId());
        if(!currency.isPresent()) {
            log.error("[" + new Date() + "] => CRYPTO CURRENCY NOT PRESENT >>>>>>>> buyRequest :: BuyController.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if(!currency.get().getIsSupported()) {
            log.error("[" + new Date() + "] => CRYPTO CURRENCY NOT SUPPORTED >>>>>>>> buyRequest :: BuyController.java");
            return new ResponseEntity<>(Codes.CURRENCY_NOT_SUPPORTED, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. transaction option that the user will use to buy
        Optional<TransactionOption> transactionOption = utilitiesService.getTransactionOption(dto.getTransactionOptIso());
        if(!transactionOption.isPresent()) {
            log.error("[" + new Date() + "] => TRANSACTION OPT NOT PRESENT >>>>>>>> buyRequest :: BuyController.java");
            return new ResponseEntity<>(Codes.CURRENCY_NOT_SUPPORTED, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // ============================== checking duskswap balance ==================================
        Optional<OverallBalance> balance = overallBalanceService.getBalanceFor(currency.get());
        if(balance == null) {
            log.error("[" + new Date() + "] => OVERALL BALANCE NOT PRESENT >>>>>>>> buyRequest :: BuyController.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        String usdXafRate = "";
        DefaultConfig config = defaultConfigService.getConfigs();
        if(config == null) {
            usdXafRate = DefaultProperties.DEFAULT_USD_XAF_BUY_RATE;
        }
        else
            usdXafRate = config.getUsdToXafBuy();

        Double estimatedAmountOfCryptoToBeReceived = buyService.estimateAmountInCryptoToBeReceived(
                user.get(),
                account,
                currency.get(),
                dto.getAmount(),
                usdXafRate
        );

        if(estimatedAmountOfCryptoToBeReceived == null) {
            log.error("[" + new Date() + "] => UNABLE TO ESTIMATED CONVERSION AMOUNT >>>>>>>> buyRequest :: BuyController.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(estimatedAmountOfCryptoToBeReceived >= Double.parseDouble(balance.get().getWithdrawalBalance())) {
            log.error("[" + new Date() + "] => UNABLE TO ESTIMATED CONVERSION AMOUNT >>>>>>>> buyRequest :: BuyController.java");
            return new ResponseEntity<>(Codes.INSUFFICIENT_BALANCE_DUSKSWAP, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // ========================= Performing the payment request via API ============================
        // >>>>> 4. payment request object creation based on user's inputs
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

        // >>>>> 5. generation of the payment URL
        String paymentAPIUsed = null;
        if(transactionOption.get().getIso().equals(DefaultProperties.ORANGE_MONEY))
            paymentAPIUsed = config.getOmAPIUsed().getApiIso();
        if(transactionOption.get().getIso().equals(DefaultProperties.MTN_MOBILE_MONEY))
            paymentAPIUsed = config.getMomoAPIUsed().getApiIso();
        if(paymentAPIUsed == null)
            paymentAPIUsed = DefaultProperties.CINETPAY_API;

        MobileMoneyPaymentResponse response = mobileMoneyOperations.performPayment(request, paymentAPIUsed);
        if(response == null) {
            log.error("[" + new Date() + "] => CANNOT PERFORM PAYMENT >>>>>>>> buyRequest :: BuyController.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // ========================= saving the transaction into a new buy object ============================
        String apiFees = response.getApiFees();
        if(
                dto.getTransactionOptIso().equals(DefaultProperties.ORANGE_MONEY) &&
                config.getOmAPIUsed() != null
        )
            apiFees = config.getOmAPIUsed().getPaymentFees();

        if(
                dto.getTransactionOptIso().equals(DefaultProperties.MTN_MOBILE_MONEY) &&
                config.getMomoAPIUsed() != null
        )
            apiFees = config.getMomoAPIUsed().getPaymentFees();

        log.info(">>>>>>>>>>>>>>>>> apiFees = " + apiFees);

        // >>>>> 6. now we create and save the buy
        Buy buy = buyService.createBuy(user.get(), account, dto.getAmount(), currency.get(), transactionOption.get(), response.getPaymentToken(), apiFees, txId);
        if(buy == null) {
            log.error("[" + new Date() + "] => CANNOT SAVE BUY >>>>>>>> buyRequest :: BuyController.java");
            return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return ResponseEntity.ok(response.getPaymentUrl());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/manual-check-status", produces = "application/json")
    public ResponseEntity<?> manuallyCheckCinetpayStatus(@RequestParam(name = "transactionId") String transactionId) throws Exception {
        if(transactionId == null || (transactionId != null && transactionId.isEmpty())) {
            log.error("[" + new Date() + "] => TRANSACTION ID NULL OR EMPTY >>>>>>>> manuallyCheckCinetpayStatus :: BuyController.java");
            return ResponseEntity.badRequest().body(Codes.INPUT_ERROR_CODE);
        }

        // >>>>> 1. we get the corresponding buy object
        Optional<Buy> buy = buyService.getByTransactionId(transactionId);
        if(!buy.isPresent()) {
            log.error("[" + new Date() + "] => CANNOT FIND CORRESPONDING BUY >>>>>>>> manuallyCheckCinetpayStatus :: BuyController.java");
            return new ResponseEntity<>(Codes.BUY_NOT_EXISTING, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 2. then we check if the status is already "confirmed"
        if(buy.get().getStatus().getName().equals(DefaultProperties.STATUS_TRANSACTION_CONFIRMED)) {
            log.info("[" + new Date() + "] => BUY ALREADY CONFIRMED >>>>>>>> manuallyCheckCinetpayStatus :: BuyController.java");
            return new ResponseEntity<>(Codes.STATUS_ALREADY_CONFIRMED_OR_INVALID, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. here we check the status of the payment on cinetpay server
        VerificationResponse verificationResponse = mobileMoneyOperations.checkPaymentStatus(buy.get().getPayToken(), CinetpayParams.SITE_ID);
        if(verificationResponse == null) {
            log.error("[" + new Date() + "] => VERIFICATION RESPONSE NULL >>>>>>>> manuallyCheckCinetpayStatus :: BuyController.java");
            return new ResponseEntity<>(Codes.NETWORK_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(verificationResponse.getCode().equals(CinetpayParams.STATUS_PAYMENT_CANCELED)) {
            buyService.updateBuyStatus(buy.get(), DefaultProperties.STATUS_TRANSACTION_INVALID);
            return new ResponseEntity<>(Codes.STATUS_ALREADY_CONFIRMED_OR_INVALID, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(verificationResponse.getCode().equals(CinetpayParams.STATUS_PAYMENT_CANCELED)) {
            buyService.updateBuyStatus(buy.get(), DefaultProperties.STATUS_TRANSACTION_INVALID);
            return ResponseEntity.ok(null);
        }

        // >>>>> 4. getting the usd-xaf exchange rate
        String usdXafRate = "";
        DefaultConfig config = defaultConfigService.getConfigs();
        if(config == null) {
            usdXafRate = DefaultProperties.DEFAULT_USD_XAF_BUY_RATE;
        }
        else
            usdXafRate = config.getUsdToXafBuy();

        // >>>>> 4. if everything is good, we update the buy object
        if(verificationResponse.getCode().equals(CinetpayParams.STATUS_PAYMENT_SUCCESS)) {

            String phoneNumber = verificationResponse.getData().getPhone_prefix() + verificationResponse.getData().getPhone_number();
            Buy savedBuy =  buyService.confirmBuy(buy.get(), usdXafRate, phoneNumber);
            if(savedBuy == null) {
                return new ResponseEntity<>(Codes.UNKNOWN_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
            }

            accountService.fundAccount(buy.get().getExchangeAccount(), buy.get().getToCurrency(), buy.get().getAmountCrypto());
            log.info("[" + new Date() + "] => CONFIRMED BUY : " + savedBuy + " >>>>>>>> manuallyCheckCinetpayStatus :: BuyController.java");
            return ResponseEntity.ok(Codes.CODE_SUCCESS);

        }

        return ResponseEntity.ok(null);

    }

    @PostMapping(value = "/check-status", produces = "application/json")
    public void checkCinetpayStatus(@RequestParam(name = "cpm_trans_id") String transactionId,
                            @RequestParam(name = "cpm_site_id") String siteId) throws Exception { // check status for cinetpay
        // input checking
        if(transactionId == null)
            return;

        // >>>>> 1. we get the corresponding buy object
        Optional<Buy> buy = buyService.getByTransactionId(transactionId);
        if(!buy.isPresent()) {
            log.error("[" + new Date() + "] => CANNOT FIND CORRESPONDING BUY >>>>>>>> checkStatus :: BuyController.java");
            return;
        }

        // >>>>> 2. then we check if the status is already "confirmed"
        if(buy.get().getStatus().getName().equals(DefaultProperties.STATUS_TRANSACTION_CONFIRMED)) {
            log.info("[" + new Date() + "] => BUY ALREADY CONFIRMED >>>>>>>> checkStatus :: BuyController.java");
            return;
        }

        // >>>>> 3. here we check the status of the payment on cinetpay server
        VerificationResponse verificationResponse = mobileMoneyOperations.checkPaymentStatus(buy.get().getPayToken(), siteId);
        if(verificationResponse == null) {
            log.error("[" + new Date() + "] => VERIFICATION RESPONSE NULL >>>>>>>> checkStatus :: BuyController.java");
            return;
        }

        if(verificationResponse.getCode().equals(CinetpayParams.STATUS_PAYMENT_CANCELED)) {
            buyService.updateBuyStatus(buy.get(), DefaultProperties.STATUS_TRANSACTION_INVALID);
            return;
        }

        // >>>>> 4. getting the usd-xaf exchange rate
        String usdXafRate = "";
        DefaultConfig config = defaultConfigService.getConfigs();
        if(config == null) {
            usdXafRate = DefaultProperties.DEFAULT_USD_XAF_BUY_RATE;
        }
        else
            usdXafRate = config.getUsdToXafBuy();

        // >>>>> 4. if everything is good, we update the buy object
        if(verificationResponse.getCode().equals(CinetpayParams.STATUS_PAYMENT_SUCCESS)) {

            String phoneNumber = verificationResponse.getData().getPhone_prefix() + verificationResponse.getData().getPhone_number();
            Buy savedBuy =  buyService.confirmBuy(buy.get(), usdXafRate, phoneNumber);
            if(savedBuy == null) {
                return;
            }

            accountService.fundAccount(buy.get().getExchangeAccount(), buy.get().getToCurrency(), buy.get().getAmountCrypto());
            log.info("[" + new Date() + "] => CONFIRMED BUY : " + savedBuy + " >>>>>>>> checkStatus :: BuyController.java");

        }

        return;
    }

}
