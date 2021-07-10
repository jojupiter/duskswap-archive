package com.dusk.duskswap.withdrawal.services;

import com.dusk.binanceExchangeRates.factories.BinanceRateFactory;
import com.dusk.binanceExchangeRates.models.BinanceRate;
import com.dusk.binanceExchangeRates.repositories.BinanceRateRepository;
import com.dusk.duskswap.account.models.AmountCurrency;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.AmountCurrencyRepository;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Status;
import com.dusk.duskswap.commons.repositories.TransactionOptionRepository;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.models.UserDetailsImpl;
import com.dusk.duskswap.withdrawal.entityDto.WithdrawalDto;
import com.dusk.duskswap.withdrawal.entityDto.WithdrawalPage;
import com.dusk.duskswap.withdrawal.models.Withdrawal;
import com.dusk.duskswap.withdrawal.repositories.WithdrawalRepository;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.dusk.duskswap.commons.repositories.StatusRepository;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
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
    private AmountCurrencyRepository amountCurrencyRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private TransactionOptionRepository transactionOptionRepository;
    @Autowired
    private BinanceRateFactory binanceRateFactory;
    @Autowired
    private JwtUtils jwtUtils;
    private Logger logger = LoggerFactory.getLogger(WithdrawalServiceImpl.class);

    @Override
    public ResponseEntity<WithdrawalPage> getAllUserWithdrawals(Integer currentPage, Integer pageSize) {
        // first we get the current authenticated user
        Optional<User> user = getCurrentUser();
        if(!user.isPresent()) {
            logger.error("[" + new Date() + "] => USER NOT PRESENT >>>>>>>> getAllUserWithdrawals :: WithdrawalServiceImpl.java");
            return null;
        }

        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        // getting the corresponding exchange account and verify if exists
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user.get());
        if(!exchangeAccount.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getAllUserWithdrawals :: DepositServiceImpl.java");
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

        Pageable pageable = PageRequest.of(currentPage, pageSize/*, Sort.by("createdDate").descending()*/);
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
    public Withdrawal createWithdrawal(WithdrawalDto wdto, User user){
        // input checking
        if(wdto == null ||
                (wdto != null && (wdto.getAmount() == null || (wdto.getAmount() != null && wdto.getAmount().isEmpty()) )
                )
        ) {
            logger.error("[" + new Date() + "] => INPUT NULL >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java ======= wdto = " + wdto);
            return null;
        }

        // we then get exchange account
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user);
        if(!exchangeAccount.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java");
            return null;
        }
        // next, it's currency
        Optional<Currency> currency = currencyRepository.findById(wdto.getCurrencyId());
        if(!currency.isPresent()) {
            logger.error("[" + new Date() + "] => CURRENCY NOT PRESENT >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java");
            return null;
        }
        // next, the amount of the corresponding currency
        Optional<AmountCurrency> amountCurrency = amountCurrencyRepository.findByAccountAndCurrencyId(exchangeAccount.get().getId(), currency.get().getId());
        if(!amountCurrency.isPresent()) {
            logger.error("[" + new Date() + "] => AMOUNT CURRENCY NOT PRESENT >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java");
            return null;
        }

        // After getting all the necessary info, we check whether or not the balance is sufficient before debiting the account
        Double amount = Double.parseDouble(wdto.getAmount());
        if(amount < 0 || amount > Double.parseDouble(amountCurrency.get().getAmount())) {
            logger.error("[" + new Date() + "] => INSUFFICIENT BALANCE >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java");
            return null;
        }
        // if sufficient amount, we create the withdrawal
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAmount(wdto.getAmount());
        withdrawal.setClientAddress(wdto.getToAddress());
        withdrawal.setCurrency(currency.get());
        withdrawal.setExchangeAccount(exchangeAccount.get());

        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_TRANSACTION_CONFIRMED);
        withdrawal.setStatus(status.get());
        Class<?> currencyBinanceClassName = binanceRateFactory.getBinanceClassFromName(currency.get().getIso());
        BinanceRate rate = binanceRateRepository.findLastCryptoUsdRecord(currencyBinanceClassName);
        if(currencyBinanceClassName == null)
        {
            logger.error("[" + new Date() + "] => CURRENCY BINANCE CLASS NAME NULL >>>>>>>> createWithdrawal :: WithdrawalServiceImpl.java");
            return null;
        }
        if(rate != null)
            withdrawal.setMarketPriceUsd(rate.getTicks().getClose());

        return withdrawalRepository.save(withdrawal);
    }

    @Override
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken || authentication == null)
            return Optional.empty();
        Long userId = ((UserDetailsImpl)authentication.getPrincipal()).getId();
        if(userId == null)
            return Optional.empty();
        return userRepository.findById(userId);
    }

}
