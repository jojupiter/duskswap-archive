package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.miscellaneous.Misc;
import com.dusk.duskswap.commons.models.*;
import com.dusk.duskswap.commons.repositories.*;
import com.dusk.duskswap.commons.services.InvoiceService;
import com.dusk.duskswap.deposit.entityDto.DepositDto;
import com.dusk.duskswap.deposit.entityDto.DepositHashCount;
import com.dusk.duskswap.deposit.entityDto.DepositHashPage;
import com.dusk.duskswap.deposit.entityDto.DepositPage;
import com.dusk.duskswap.deposit.models.Deposit;
import com.dusk.duskswap.deposit.models.DepositHash;
import com.dusk.duskswap.deposit.repositories.DepositHashRepository;
import com.dusk.duskswap.deposit.repositories.DepositRepository;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DepositServiceImpl implements DepositService {

    @Autowired
    private DepositRepository depositRepository;
    @Autowired
    private DepositHashRepository depositHashRepository;
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

    @Override
    public ResponseEntity<DepositPage> getAllUserDeposits(User user, Integer currentPage, Integer pageSize) {
        // input checking
        if(
                user == null ||
                (
                        user != null && (user.getEmail() == null || (user.getEmail() != null && user.getEmail().isEmpty()))
                )
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> getAllUserDeposits :: DepositServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }
        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        // getting the corresponding exchange account and verify if exists
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user);
        if(!exchangeAccount.isPresent()) {
            log.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getAllUserDeposits :: DepositServiceImpl.java");
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
    public ResponseEntity<DepositPage> getAllDeposits(Integer currentPage, Integer pageSize) {

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
    public Optional<Deposit> getDepositById(Long depositId) {
        if(depositId == null) {
            log.error("[" + new Date() + "] => INPUT INCORRECT (DEPOSIT ID NULL) >>>>>>>> getDepositById :: DepositServiceImpl.java");
            return Optional.empty();
        }
        return depositRepository.findById(depositId);
    }

    @Override
    public Optional<Deposit> getDepositByInvoiceId(String invoiceId) {
        if(invoiceId == null || (invoiceId != null &&  invoiceId.isEmpty())) {
            log.error("[" + new Date() + "] => INPUT INCORRECT (INVOICE ID NULL) >>>>>>>> getDepositByInvoiceId :: DepositServiceImpl.java");
            return Optional.empty();
        }
        return depositRepository.findByInvoiceId(invoiceId);
    }

    @Transactional
    @Override
    public ResponseEntity<String> createCryptoDeposit(User user, DepositDto dto) throws Exception {
        // input checking
        if(dto == null || (dto != null && dto.getCurrencyId() == null)
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT >>>>>>>> createDeposit :: DepositServiceImpl.java" +
                    " ========= DepositDto = " + dto);
            return ResponseEntity.badRequest().body(null);
        }

        // >>>>> 1. If the amount is null, empty or negative, we set it to a default value
        if(
                dto.getAmount() == null ||
                (dto.getAmount() != null && dto.getAmount().isEmpty()) ||
                (dto.getAmount() != null && !dto.getAmount().isEmpty() && Double.parseDouble(dto.getAmount()) < 0)
        )
            dto.setAmount(DefaultProperties.DEPOSIT_DEFAULT_VALUE);

        // >>>>> 2. we get the exchange account
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user);
        if(!exchangeAccount.isPresent()) {
            log.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 3. We check the invoice Id in exchange account, count the number of deposit associated with it and decide whether or not to create another invoice
        if(exchangeAccount.get().getInvoiceId() != null && !exchangeAccount.get().getInvoiceId().isEmpty()) {
            DepositHashCount depositHashCount = countDepositHashes(exchangeAccount.get().getInvoiceId());
            if(
                    depositHashCount != null && depositHashCount.getTotalHashCount() != null &&
                    (
                            depositHashCount.getTotalHashCount() >= 0 &&
                            depositHashCount.getTotalHashCount() <= DefaultProperties.MAX_NUMBER_OF_TRANSACTION_FOR_INVOICE
                    )
            ) {
                Optional<Deposit> deposit = depositRepository.findByInvoiceId(exchangeAccount.get().getInvoiceId());
                return ResponseEntity.ok(deposit.get().getToAddress());
            }

        }

        // >>>>> 4. then, if  we create an invoice for the deposit
        Optional<Currency> currency = currencyRepository.findById(dto.getCurrencyId());
        if(!currency.isPresent()) {
            log.error("[" + new Date() + "] => CURRENCY ACCOUNT NOT PRESENT >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Invoice invoice = new Invoice();
        invoice.setAmount(dto.getAmount());
        invoice.setCurrency(currency.get().getIso());
        Checkout checkout = new Checkout();
        List<String> paymentMethods = new ArrayList<>();
        paymentMethods.add(currency.get().getIso());
        checkout.setPaymentMethods(paymentMethods);
        checkout.setSpeedPolicy(DefaultProperties.BTCPAY_INVOICE_MEDIUM_SPEED);
        invoice.setCheckout(checkout);

        ResponseEntity<Invoice> invoiceResponse = invoiceService.createInvoice(invoice);

        if(invoiceResponse.getStatusCode() != HttpStatus.OK)
            return new ResponseEntity<>(null, invoiceResponse.getStatusCode());

        // >>>>> 5. creation of the deposit object to be saved
        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_TRANSACTION_CRYPTO_NEW);
        if(!status.isPresent()) {
            log.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> createDeposit :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // >>>>> 6. If the deposit's amount is within the authorized bounds, then we proceed to the deposit creation

        Deposit deposit = new Deposit();
        deposit.setStatus(status.get());
        deposit.setExchangeAccount(exchangeAccount.get());
        deposit.setCurrency(currency.get());
        deposit.setInvoiceId(invoiceResponse.getBody().getId());
        deposit.setAmount(dto.getAmount());

        Deposit savedDeposit = depositRepository.save(deposit);

        // >>>>> 7. finally return the source code of the invoice
        String invoicePageSource = "";
        invoicePageSource = Misc.getWebPabeSource(invoiceResponse.getBody().getCheckoutLink());

        String depositIdString = "@@" + savedDeposit.getId(); // we append this to the page's source code to make it easier for front end

        return ResponseEntity.ok(invoicePageSource + depositIdString);
    }

    @Transactional
    @Override
    public Deposit updateDepositStatus(Deposit deposit, String statusString, String paidAmount) throws Exception{
        if(deposit == null ||
           (statusString != null && statusString.isEmpty()) || statusString == null
        ) {
            log.error("[" + new Date() + "] => INPUTS INCORRECT >>>>>>>> updateDepositStatus :: DepositServiceImpl.java");
            throw new Exception("[" + new Date() + "] => INPUTS INCORRECT >>>>>>>> updateDepositStatus :: DepositServiceImpl.java");
            //return null;
        }

        Optional<Status> status = statusRepository.findByName(statusString);
        if(!status.isPresent()) {
            log.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> updateDepositStatus :: DepositServiceImpl.java");
            throw new Exception("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> updateDepositStatus :: DepositServiceImpl.java");
            //return null;
        }

        deposit.setStatus(status.get());
        deposit.setDepositDate(new Date());
        if(paidAmount != null && !paidAmount.isEmpty())
            deposit.setAmount(paidAmount);

        return depositRepository.save(deposit);
    }

    @Override
    public ResponseEntity<Boolean> updateDestinationAddress(Long depositId, String toAddress) {
        // input checking
        if(depositId == null || toAddress == null || (toAddress != null && toAddress.isEmpty())) {
            log.error("[" + new Date() + "] => INPUT NULL OR EMPTY >>>>>>>> updateDestinationAddress :: DepositServiceImpl.java ====== " +
                    "depositId = " + depositId + ", toAddress = " + toAddress);
            return ResponseEntity.badRequest().body(false);
        }

        // we get the corresponding deposit
        Optional<Deposit> deposit = depositRepository.findById(depositId);
        if(!deposit.isPresent()) {
            log.error("[" + new Date() + "] => DEPOSIT NOT PRESENT >>>>>>>> updateDestinationAddress :: DepositServiceImpl.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // here we update
        deposit.get().setToAddress(toAddress);
        depositRepository.save(deposit.get());

        // after that, we update account invoice id
        deposit.get().getExchangeAccount().setInvoiceId(deposit.get().getInvoiceId());
        exchangeAccountRepository.save(deposit.get().getExchangeAccount());

        return ResponseEntity.ok(true);
    }


    // ======================================== DEPOSIT HASH =======================================================
    @Override
    public ResponseEntity<DepositHashPage> getAllUserDepositHashes(ExchangeAccount account, Integer currentPage, Integer pageSize) {
        // input checking
        if(account == null) {
            log.error("[" + new Date() + "] => INPUT NULL (ACCOUNT) >>>>>>>> getAllUserDepositHashes :: DepositServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<DepositHash> depositHashes = depositHashRepository.findByExchangeAccount(account.getId(), pageable);
        if(depositHashes.hasContent()) {
            DepositHashPage depositHashPage = new DepositHashPage();
            depositHashPage.setCurrentPage(depositHashes.getNumber());
            depositHashPage.setTotalItems(depositHashes.getTotalElements());
            depositHashPage.setTotalNumberPages(depositHashes.getTotalPages());
            depositHashPage.setDepositHashes(depositHashes.getContent());

            return ResponseEntity.ok(depositHashPage);
        }

        return ResponseEntity.ok(null);
    }

    @Override
    public Optional<DepositHash> getDepositHashByTransaction(String transactionHash) {
        // input checking
        if(transactionHash == null || (transactionHash != null &&  transactionHash.isEmpty())) {
            log.error("[" + new Date() + "] => INPUT NULL (TRANSACTION HASH) >>>>>>>> createDepositHash :: DepositServiceImpl.java");
            return Optional.empty();
        }

        return depositHashRepository.findByTransactionHash(transactionHash);
    }

    @Override
    public Boolean createDepositHash(List<InvoicePayment> invoicePayments, Deposit deposit) throws Exception {
        // input checking
        if(
                invoicePayments == null || (invoicePayments != null && invoicePayments.isEmpty()) ||
                deposit == null
        ) {
            log.error("[" + new Date() + "] => INPUT NULL (INVOICE PAYMENTS OR DEPOSIT) >>>>>>>> createDepositHash :: DepositServiceImpl.java" +
                    " ============== invoicePayments = " + invoicePayments + ", deposit = " + deposit);
            return false;
        }

        List<DepositHash> depositHashes = new ArrayList<>();

        for(InvoicePayment invoicePayment : invoicePayments) {

            for(int i = invoicePayment.getPayments().size() - 1; i >= 0; i--) {// reverse looping on payments
                Payment payment = invoicePayment.getPayments().get(i);
                if(depositHashRepository.existsByTransactionHash(payment.getId())) {
                    DepositHash depositHash = new DepositHash();
                    depositHash.setTransactionHash(payment.getId());
                    depositHash.setAmount(payment.getValue());
                    depositHash.setToDepositAddress(invoicePayment.getDestination());
                    depositHash.setDeposit(deposit);
                    depositHash.setFromDepositAddress(payment.getDestination());

                    Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_TRANSACTION_CRYPTO_RADICAL + payment.getStatus());
                    if(!status.isPresent()) {
                        log.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> createDepositHash :: DepositServiceImpl.java");
                        throw new Exception("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> createDepositHash :: DepositServiceImpl.java");
                    }
                    depositHash.setStatus(status.get());
                    depositHashes.add(depositHash);
                    break; // TODO: Revisit this, because we supposed here just one payment comes at time
                }
            }
        }

        depositHashRepository.saveAll(depositHashes);

        return true;
    }

    @Override
    public DepositHash updateDepositHashStatus(DepositHash depositHash, String statusString) {
        // input checking
        if(
                depositHash == null ||
                statusString == null || (statusString != null || statusString.isEmpty())
        ) {
            log.error("[" + new Date() + "] => INPUT NULL (STATUS OR DEPOSIT HASH) >>>>>>>> updateDepositHashStatus :: DepositServi ceImpl.java");
            return null;
        }

        Optional<Status> status = statusRepository.findByName(statusString);
        if(!status.isPresent()) {
            log.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> updateDepositHashStatus :: DepositServiceImpl.java");
            return null;
        }

        if(!depositHash.getStatus().getName().equals(status.get().getName())) {
            depositHash.setStatus(status.get());
            depositHashRepository.save(depositHash);
        }

        return null;
    }

    @Override
    public DepositHashCount countDepositHashes(String invoiceId) {
        if(invoiceId == null || (invoiceId != null && invoiceId.isEmpty())) {
            log.error("[" + new Date() + "] => INPUT ERROR (INVOICE ID NULL OR EMPTY) >>>>>>>> countDepositHashes :: DepositServiceImpl.java");
            return null;
        }

        // >>>>> 1. getting the deposit using invoiceId
        Optional<Deposit> deposit = depositRepository.findByInvoiceId(invoiceId);
        if(!deposit.isPresent()) {
            log.error("[" + new Date() + "] => DEPOSIT NOT PRESENT >>>>>>>> countDepositHashes :: DepositServiceImpl.java");
            return null;
        }

        // >>>>> 2. then we invoke count method from deposit hash repository
        DepositHashCount depositHashCount = new DepositHashCount();
        depositHashCount.setDeposit(deposit.get());
        depositHashCount.setTotalHashCount(depositHashRepository.countTotalDepositHash(deposit.get().getId()));

        return depositHashCount;
    }

}
