package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.miscellaneous.Misc;
import com.dusk.duskswap.commons.models.*;
import com.dusk.duskswap.commons.repositories.*;
import com.dusk.duskswap.commons.services.InvoiceService;
import com.dusk.duskswap.deposit.entityDto.DepositDto;
import com.dusk.duskswap.deposit.entityDto.DepositPage;
import com.dusk.duskswap.deposit.entityDto.DepositResponseDto;
import com.dusk.duskswap.deposit.models.Deposit;
import com.dusk.duskswap.deposit.repositories.DepositRepository;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DepositServiceImpl implements DepositService {

    @Autowired
    private DepositRepository depositRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ExchangeAccountRepository exchangeAccountRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private PricingRepository pricingRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private JwtUtils jwtUtils;
    private Logger logger = LoggerFactory.getLogger(DepositServiceImpl.class);

    @Override
    public ResponseEntity<DepositPage> getAllUserDeposits(String userToken, Integer currentPage, Integer pageSize) {
        // input checking
        if(userToken == null || (userToken != null && userToken.isEmpty())) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> getAllUserDeposits :: DepositServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }
        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        String userEmail = jwtUtils.getEmailFromJwtToken(userToken);

        if(userEmail == null || (userEmail != null && userEmail.isEmpty())) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty), Can't get email from token >>>>>>>> getAllUserDeposits :: DepositServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        // getting the corresponding user and verify if exists
        Optional<User> user = userRepository.findByEmail(userEmail);
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserDeposits :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // getting the corresponding exchange account and verify if exists
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user.get());
        if(!exchangeAccount.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getAllUserDeposits :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by("createdDate").descending());
        Page<Deposit> deposits = depositRepository.findByExchangeAccount(exchangeAccount.get(), pageable);

        if(deposits.hasContent()) {

            DepositPage depositPage = new DepositPage();
            depositPage.setCurrentPage(deposits.getNumber());
            depositPage.setTotalItems(deposits.getTotalElements());
            depositPage.setTotalNumberPages(deposits.getTotalPages());
            depositPage.setDeposits(deposits.getContent());

            return ResponseEntity.ok(depositPage);
        }

        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<DepositPage> getAllUserDeposits(Integer currentPage, Integer pageSize) {

        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Deposit> deposits = depositRepository.findAll(pageable);
        if(deposits.hasContent()) {
            DepositPage depositPage = new DepositPage();
            depositPage.setCurrentPage(deposits.getNumber());
            depositPage.setTotalItems(depositPage.getTotalItems());
            depositPage.setTotalNumberPages(depositPage.getTotalNumberPages());
            depositPage.setDeposits(deposits.getContent());

            return ResponseEntity.ok(depositPage);
        }
        return ResponseEntity.ok(null);
    }

    @Override
    public Deposit getDepositByInvoiceId(String invoiceId) {
        if(invoiceId == null)
            return null;
        return depositRepository.findByInvoiceId(invoiceId).get();
    }

    @Transactional
    @Override
    public ResponseEntity<DepositResponseDto> createCryptoDeposit(DepositDto dto) {
        // input checking
        if(dto == null ||
                (dto != null &&
                        (
                                dto.getAmount() == null || (dto.getAmount() != null && dto.getAmount().isEmpty()) ||
                                dto.getJwtToken() == null || (dto.getJwtToken() != null && dto.getJwtToken().isEmpty()) ||
                                dto.getCurrencyId() == null
                         )
                 )
        ) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT >>>>>>>> createDeposit :: DepositServiceImpl.java" +
                    " ========= DepositDto = " + dto);
            return ResponseEntity.badRequest().body(null);
        }

        // First we check if the user exists and has already an exchange account
        Optional<User> user = userRepository.findByEmail(jwtUtils.getEmailFromJwtToken(dto.getJwtToken()));
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if(user.get().getLevel() == null) {
            logger.error("[" + new Date() + "] => USER HAS NO CORRESPONDING LEVEL >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user.get());
        if(!exchangeAccount.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        //then, we create an invoice for the deposit
        Optional<Currency> currency = currencyRepository.findById(dto.getCurrencyId());
        if(!currency.isPresent()) {
            logger.error("[" + new Date() + "] => CURRENCY ACCOUNT NOT PRESENT >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Invoice invoice = new Invoice();
        invoice.setAmount(dto.getAmount());
        invoice.setCurrency(currency.get().getIso());
        Checkout checkout = new Checkout();
        List<String> paymentMethods = new ArrayList<>();
        paymentMethods.add(currency.get().getIso()); // TODO: REVIEW THIS, SEE IF WE HAVE TO USE dto.transactionOpt instead
        invoice.setCheckout(checkout);
        ResponseEntity<Invoice> invoiceResponse = invoiceService.createInvoice(invoice);

        if(invoiceResponse.getStatusCode() != HttpStatus.OK)
            return new ResponseEntity<>(null, invoiceResponse.getStatusCode());

        // creation of the deposit object to be saved
        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_TRANSACTION_CRYPTO_NEW);
        if(!status.isPresent()) {
            logger.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // Then we check if it's possible for the user to make a deposit by looking at the min and max authorized pricing value
        Optional<Pricing> pricing = pricingRepository.findByLevelAndCurrency(user.get().getLevel(), currency.get());
        if(!pricing.isPresent()) {
            logger.error("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(
                Double.parseDouble(dto.getAmount()) < Double.parseDouble(pricing.get().getDepositMin()) ||
                Double.parseDouble(dto.getAmount()) > Double.parseDouble(pricing.get().getDepositMax())
        ) {
            logger.error("[" + new Date() + "] => CAN'T MAKE DEPOSIT (The amount is too high/low for the authorized amount) >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // If the deposit's amount is within the authorized bounds, then we proceed to the deposit creation

        Deposit deposit = new Deposit();
        deposit.setStatus(status.get());
        deposit.setExchangeAccount(exchangeAccount.get());
        deposit.setCurrency(currency.get());
        deposit.setInvoiceId(invoiceResponse.getBody().getId());
        deposit.setAmount(dto.getAmount());

        Deposit savedDeposit = depositRepository.save(deposit);


        // finally return the source code of the invoice
        DepositResponseDto responseDto = new DepositResponseDto();
        responseDto.setDepositId(savedDeposit.getId());
        String invoicePageSource = "";
        try {
            invoicePageSource = Misc.getWebPabeSource(invoiceResponse.getBody().getCheckoutLink());
            responseDto.setInvoiceSourceCode(invoicePageSource);
        }
        catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return ResponseEntity.ok(responseDto);
    }

    @Override
    public Deposit updateDepositStatus(Long depositId, String statusString) {
        if(depositId == null ||
           (statusString != null && statusString.isEmpty()) || statusString == null
        ) {
            logger.error("[" + new Date() + "] => INPUTS INCORRECT >>>>>>>> updateDepositStatus :: DepositServiceImpl.java");
            return null;
        }

        Optional<Deposit> deposit = depositRepository.findById(depositId);
        if(!deposit.isPresent()) {
            logger.error("[" + new Date() + "] => DEPOSIT NOT PRESENT >>>>>>>> updateDepositStatus :: DepositServiceImpl.java");
            return null;
        }
        Optional<Status> status = statusRepository.findByName(statusString);
        if(!status.isPresent()) {
            logger.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> updateDepositStatus :: DepositServiceImpl.java");
            return null;
        }

        deposit.get().setStatus(status.get());

        return depositRepository.save(deposit.get());
    }

    @Override
    public ResponseEntity<Boolean> updateDestinationAddress(Long depositId, String toAddress) {
        // input checking
        if(depositId == null || toAddress == null || (toAddress != null && toAddress.isEmpty())) {
            logger.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> updateDestinationAddress :: DepositServiceImpl.java ====== " +
                    "depositId = " + depositId + ", toAddress = " + toAddress);
            return ResponseEntity.badRequest().body(false);
        }

        Optional<Deposit> deposit = depositRepository.findById(depositId);
        if(!deposit.isPresent()) {
            logger.error("[" + new Date() + "] => DEPOSIT NOT PRESENT >>>>>>>> updateDestinationAddress :: DepositServiceImpl.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        deposit.get().setToAddress(toAddress);

        depositRepository.save(deposit.get());

        return ResponseEntity.ok(true);
    }
}
