package com.dusk.duskswap.exchange.repositories;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.exchange.models.Exchange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ExchangeRepository extends PagingAndSortingRepository<Exchange, Long> {

    Page<Exchange> findAll(Pageable pageable);
    Page<Exchange> findByExchangeAccount(ExchangeAccount exchangeAccount, Pageable pageable);

}
