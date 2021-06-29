package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.miscellaneous.Misc;
import com.dusk.duskswap.commons.models.Checkout;
import com.dusk.duskswap.commons.models.Invoice;
import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.repositories.TransactionOptionRepository;
import com.dusk.duskswap.commons.services.InvoiceService;
import com.dusk.duskswap.deposit.entityDto.DepositDto;
import com.dusk.duskswap.deposit.models.Deposit;
import com.dusk.duskswap.deposit.repositories.DepositRepository;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Status;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.dusk.duskswap.commons.repositories.StatusRepository;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
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
    private CurrencyRepository currencyRepository;
    @Autowired
    private TransactionOptionRepository transactionOptionRepository;
    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private JwtUtils jwtUtils;
    private Logger logger = LoggerFactory.getLogger(DepositServiceImpl.class);

    @Override
    public ResponseEntity<List<Deposit>> getAllUserDeposits(String userToken) {
        // input checking
        if(userToken == null || (userToken != null && userToken.isEmpty())) {
            logger.error("INPUT INCORRECT (null or empty) >>>>>>>> getAllUserDeposits :: DepositServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        String userEmail = jwtUtils.getEmailFromJwtToken(userToken);

        if(userEmail == null || (userEmail != null && userEmail.isEmpty())) {
            logger.error("INPUT INCORRECT (null or empty), Can't get email from token >>>>>>>> getAllUserDeposits :: DepositServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }


        // getting the corresponding user and verify if exists
        Optional<User> user = userRepository.findByEmail(userEmail);
        if(!user.isPresent()) {
            logger.error("USER NOT PRESENT >>>>>>>> getAllUserDeposits :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // getting the corresponding exchange account and verify if exists
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user.get());
        if(!exchangeAccount.isPresent()) {
            logger.error("EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getAllUserDeposits :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(depositRepository.findByExchangeAccount(exchangeAccount.get()));
    }

    @Override
    public ResponseEntity<List<Deposit>> getAllUserDeposits() {
        return ResponseEntity.ok(depositRepository.findAll());
    }

    @Override
    public Deposit getDepositByInvoiceId(String invoiceId) {
        if(invoiceId == null)
            return null;
        return depositRepository.findByInvoiceId(invoiceId).get();
    }

    @Transactional
    @Override
    public ResponseEntity<String> createCryptoDeposit(DepositDto dto) {
        // input checking
        if(dto == null ||
                (dto != null &&
                        (
                                dto.getAmount() == null || (dto.getAmount() != null && dto.getAmount().isEmpty()) ||
                                dto.getJwtToken() == null || (dto.getJwtToken() != null && dto.getJwtToken().isEmpty()) ||
                                dto.getCurrencyId() == null ||
                                dto.getTransactionOptId() == null
                         )
                 )
        )
            return ResponseEntity.badRequest().body(null);

        // First we check if the user exists and has already an exchange account
        Optional<User> user = userRepository.findByEmail(jwtUtils.getEmailFromJwtToken(dto.getJwtToken()));
        if(!user.isPresent()) {
            logger.error("USER NOT PRESENT >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user.get());
        if(!exchangeAccount.isPresent()) {
            logger.error("EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        //then, we create an invoice for the deposit
        Optional<Currency> currency = currencyRepository.findById(dto.getCurrencyId());
        if(!currency.isPresent()) {
            logger.error("CURRENCY ACCOUNT NOT PRESENT >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Invoice invoice = new Invoice();
        invoice.setAmount(dto.getAmount().toString());
        invoice.setCurrency(currency.get().getIso());
        Checkout checkout = new Checkout();
        List<String> paymentMethods = new ArrayList<>();
        paymentMethods.add(currency.get().getIso()); // TODO: REVIEW THIS, SEE IF WE HAVE TO USE dto.transactionOpt instead
        invoice.setCheckout(checkout);
        ResponseEntity<Invoice> invoiceResponse = invoiceService.createInvoice(invoice);

        if(invoiceResponse.getStatusCode() != HttpStatus.OK)
            return new ResponseEntity<>(null, invoiceResponse.getStatusCode());

        // creation of the deposit object to be saved
        Optional<Status> status = statusRepository.findByName("TRANSACTION_CRYPTO_New");
        if(!status.isPresent()) {
            logger.error("STATUS NOT PRESENT >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<TransactionOption> transactionOption = transactionOptionRepository.findById(dto.getTransactionOptId());
        if(!transactionOption.isPresent()) {
            logger.error("TRANSACTION OPT NOT PRESENT >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Deposit deposit = new Deposit();
        deposit.setStatus(status.get());
        deposit.setAmount(dto.getAmount());
        deposit.setExchangeAccount(exchangeAccount.get());
        deposit.setCurrency(currency.get());
        deposit.setTransactionOption(transactionOption.get());
        deposit.setInvoiceId(invoiceResponse.getBody().getId());

        depositRepository.save(deposit);

        // after that, we increment the exchange account
        // TODO: CHECK THE LIMIT OF TRANSACTION BASED ON USER'S LEVEL

        // finally return the source code of the invoice
        String invoicePageSource = "";
        try {
            invoicePageSource = Misc.getWebPabeSource(invoiceResponse.getBody().getCheckoutLink());
        }
        catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return ResponseEntity.ok(invoicePageSource);
    }

    @Override
    public Deposit updateDepositStatus(Long depositId, String statusString) {
        if(depositId == null ||
           (statusString != null && statusString.isEmpty()) || statusString == null
        ) {
            logger.error("INPUTS INCORRECT >>>>>>>> updateDepositStatus :: DepositServiceImpl.java");
            return null;
        }

        Optional<Deposit> deposit = depositRepository.findById(depositId);
        if(!deposit.isPresent()) {
            logger.error("DEPOSIT NOT PRESENT >>>>>>>> updateDepositStatus :: DepositServiceImpl.java");
            return null;
        }
        Optional<Status> status = statusRepository.findByName(statusString);
        if(!status.isPresent()) {
            logger.error("STATUS NOT PRESENT >>>>>>>> updateDepositStatus :: DepositServiceImpl.java");
            return null;
        }

        deposit.get().setStatus(status.get());

        return depositRepository.save(deposit.get());
    }

}
