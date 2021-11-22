package com.dusk.duskswap.withdrawal.services;

import com.dusk.binanceExchangeRates.factories.BinanceRateFactory;
import com.dusk.binanceExchangeRates.repositories.BinanceRateRepository;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.duskswap.administration.services.OverallBalanceService;
import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.miscellaneous.Utilities;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Pricing;
import com.dusk.duskswap.commons.models.Status;
import com.dusk.duskswap.commons.repositories.PricingRepository;
import com.dusk.duskswap.commons.repositories.TransactionOptionRepository;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.withdrawal.entityDto.WithdrawalDto;
import com.dusk.duskswap.withdrawal.entityDto.WithdrawalPage;
import com.dusk.duskswap.withdrawal.models.Withdrawal;
import com.dusk.duskswap.withdrawal.repositories.WithdrawalRepository;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.dusk.duskswap.commons.repositories.StatusRepository;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class WithdrawalServiceImpl implements WithdrawalService {

    @Autowired
    private WithdrawalRepository withdrawalRepository;
    @Autowired
    private BinanceRateRepository binanceRateRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ExchangeAccountRepository exchangeAccountRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private PricingRepository pricingRepository;
    @Autowired
    private TransactionOptionRepository transactionOptionRepository;
    @Autowired
    private BinanceRateFactory binanceRateFactory;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private OverallBalanceService overallBalanceService;

    @Override
    public ResponseEntity<WithdrawalPage> getAllUserWithdrawals(User user, Integer currentPage, Integer pageSize) {

        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        // getting the corresponding exchange account and verify if exists
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user);
        if(!exchangeAccount.isPresent()) {
            log.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getAllUserWithdrawals :: DepositServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // if account exists, we get the request's results
        Pageable pageable = PageRequest.of(currentPage, pageSize/*, Sort.by("createdDate").descending()*/);
        Page<Withdrawal> withdrawals = withdrawalRepository.findByExchangeAccount(exchangeAccount.get(), pageable);

        if(withdrawals.hasContent()) {
            WithdrawalPage withdrawalPage = new WithdrawalPage();
            withdrawalPage.setCurrentPage(withdrawals.getNumber());
            withdrawalPage.setTotalItems(withdrawals.getTotalElements());
            withdrawalPage.setTotalNumberPages(withdrawals.getTotalPages());
            withdrawalPage.setWithdrawalList(withdrawals.getContent());

            return ResponseEntity.ok(withdrawalPage);
        }

        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<WithdrawalPage> getAllWithdrawals(Integer currentPage, Integer pageSize) {

        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Withdrawal> withdrawals = withdrawalRepository.findAll(pageable);

        if(withdrawals.hasContent()) {
            WithdrawalPage withdrawalPage = new WithdrawalPage();
            withdrawalPage.setCurrentPage(withdrawals.getNumber());
            withdrawalPage.setTotalItems(withdrawals.getTotalElements());
            withdrawalPage.setTotalNumberPages(withdrawals.getTotalPages());
            withdrawalPage.setWithdrawalList(withdrawals.getContent());

            return ResponseEntity.ok(withdrawalPage);
        }

        return ResponseEntity.ok(null);
    }

    @Override
    @Transactional
    public Withdrawal createWithdrawal(WithdrawalDto wdto, Currency currency,  User user, ExchangeAccount exchangeAccount) throws Exception { // in this, we create the withdrawal without saving it. It will be saved later is the caller's code using the saveWithdrawal function
        // input checking
        if(
                wdto == null ||
                (wdto != null && (wdto.getAmount() == null || (wdto.getAmount() != null && wdto.getAmount().isEmpty()))) ||
                 currency == null ||
                 user == null ||
                 exchangeAccount == null
        ) {
            log.error("[" + new Date() + "] => INPUT NULL >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java ======= wdto = " + wdto + ", user = " + user + ", currency = " + currency + ", account = " + exchangeAccount);
            throw new Exception("[" + new Date() + "] => INPUT NULL >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java ======= wdto = " + wdto + ", user = " + user + ", currency = " + currency + ", account = " + exchangeAccount);
            //return null;
        }

        // ============================= getting the necessary elements =============================

        // >>>>> 2. we check according to the pricing, if the user is able to make
        Optional<Pricing> pricing = pricingRepository.findByLevelAndCurrency(user.getLevel(), currency);
        if(!pricing.isPresent()) {
            log.error("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java");
            throw new Exception("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java");
        }

        // we check if the amount respects the boundaries in pricing
        if(
                Double.parseDouble(wdto.getAmount()) > Double.parseDouble(pricing.get().getWithdrawalMax()) ||
                Double.parseDouble(wdto.getAmount()) < Double.parseDouble(pricing.get().getWithdrawalMin())
        ) {
            log.error("[" + new Date() + "] => INSERTED AMOUNT OUT OF BOUND (The amount is too high/low for the authorized amount) >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java");
            throw new Exception("[" + new Date() + "] => INSERTED AMOUNT OUT OF BOUND (The amount is too high/low for the authorized amount) >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java");
        }
        // >>>>> 3. we get the status "confirmed"
        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_TRANSACTION_CONFIRMED);
        if(!status.isPresent()) {
            log.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java");
            throw new Exception("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java");
        }

        // ============================== fees calculation ================================
        Double duskFees = 0.0;
        if(pricing.get().getTypeWithdrawal().equals(DefaultProperties.PRICING_TYPE_PERCENTAGE)) {
            duskFees = Double.parseDouble(pricing.get().getWithdrawalFees()) *
                       Double.parseDouble(wdto.getAmount());
        }
        if(pricing.get().getTypeWithdrawal().equals(DefaultProperties.PRICING_TYPE_FIX)) {
            duskFees = Double.parseDouble(pricing.get().getWithdrawalFees());
        }

        // once having fees we check if it will be possible to perform the withdrawal with the available amount we have
        Double estimatedFees = Utilities.estimateNetworkFees(currency.getIso());
        Double availableAmount = Double.parseDouble(overallBalanceService.getAvailableBalanceFor(currency, 1));
        if(availableAmount <= estimatedFees + duskFees + Double.parseDouble(wdto.getAmount())) {
            log.error("[" + new Date() + "] => DUSK INSUFFICIENT BALANCE >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java");
            throw new Exception("[" + new Date() + "] => DUSK INSUFFICIENT BALANCE >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java");
        }

        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setWithdrawalDate(new Date());
        withdrawal.setAmount(wdto.getAmount());
        withdrawal.setClientAddress(wdto.getToAddress());
        withdrawal.setCurrency(currency);
        withdrawal.setExchangeAccount(exchangeAccount);
        withdrawal.setDuskFeesCrypto(Double.toString(duskFees));

        return withdrawal;
    }

    @Override
    public Withdrawal saveWithdrawal(Withdrawal withdrawal) {
        // input checking
        if(withdrawal == null) {
            log.error("[" + new Date() + "] => WITHDRAWAL OBJECT NULL >>>>>>>> saveWithdrawal :: WithdrawalServiceImpl.java");
            return null;
        }
        return withdrawalRepository.save(withdrawal);
    }

    @Override
    public void deleteWithdrawal(Long withdrawalId) {
        if(withdrawalId == null)
            return;
        withdrawalRepository.deleteById(withdrawalId);
    }
}
