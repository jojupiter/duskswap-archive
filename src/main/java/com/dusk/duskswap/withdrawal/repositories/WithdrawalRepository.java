package com.dusk.duskswap.withdrawal.repositories;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.withdrawal.models.Withdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface WithdrawalRepository extends PagingAndSortingRepository<Withdrawal, Long> {
    Page<Withdrawal> findAll(Pageable pageable);
    Page<Withdrawal> findByExchangeAccount(ExchangeAccount exchangeAccount, Pageable pageable);
}
