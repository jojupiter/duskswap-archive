package com.dusk.duskswap.account.services;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.shared.usersManagement.models.User;
import com.dusk.shared.usersManagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private ExchangeAccountRepository exchangeAccountRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseEntity<ExchangeAccount> createExchangeAccount(String userEmail) {
        return null;
    }

    @Override
    public ResponseEntity<ExchangeAccount> getExchangeAccount(String emailUser) {
        return null;
    }

    @Override
    public ResponseEntity<List<ExchangeAccount>> getAllExchangeAccounts() {
        return null;
    }

    @Override
    public ExchangeAccount fundAccount(Double amount, String currency, String emailUser) {
        return null;
    }

    @Override
    public ExchangeAccount debitAccount(Double amount, String currency, String emailUser) {
        return null;
    }
}
