package com.dusk.duskswap.deposit.repositories;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.deposit.models.Buy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface BuyRepository extends PagingAndSortingRepository<Buy, Long> {
    Optional<Buy> findByPayToken(String payToken);

    Page<Buy> findByExchangeAccount(ExchangeAccount exchangeAccount, Pageable pageable);
    Page<Buy> findAll(Pageable pageable);

}
