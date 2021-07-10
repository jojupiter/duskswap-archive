package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.repositories.TransactionOptionRepository;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.usersManagement.models.UserDetailsImpl;
import com.dusk.duskswap.usersManagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UtilitiesServiceImpl implements UtilitiesService {

    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private TransactionOptionRepository transactionOptionRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    @Override
    public List<Currency> getAllSupportedCurrencies() {
        return currencyRepository.findByIsSupported(true);
    }

    @Override
    public List<TransactionOption> getAllSupportedTransactionOptions() {
        return transactionOptionRepository.findByIsSupported(true);
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
