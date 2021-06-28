package com.dusk.duskswap.withdrawal.services;

import com.dusk.binanceExchangeRates.repositories.BinanceRateRepository;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.duskswap.application.securityConfigs.JwtUtils;
import com.dusk.duskswap.commons.repositories.TransactionOptionRepository;
import com.dusk.duskswap.withdrawal.entityDto.WithdrawalDto;
import com.dusk.duskswap.withdrawal.models.Withdrawal;
import com.dusk.duskswap.withdrawal.repositories.WithdrawalRepository;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.dusk.duskswap.commons.repositories.StatusRepository;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

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
    private TransactionOptionRepository transactionOptionRepository;
    @Autowired
    private JwtUtils jwtUtils;
    private Logger logger = LoggerFactory.getLogger(WithdrawalServiceImpl.class);

    @Override
    public ResponseEntity<List<Withdrawal>> getAllUserWithdrawals(String userEmail) {
        return null;
    }

    @Override
    public ResponseEntity<List<Withdrawal>> getAllWithdrawals() {
        return null;
    }

    @Override
    public ResponseEntity<Withdrawal> createWithdrawal(WithdrawalDto wdto) {
        return null;
    }

    @Override
    public Withdrawal updateWithdrawalStatus(Long withdrawalId, String statusString) {
        return null;
    }

}
