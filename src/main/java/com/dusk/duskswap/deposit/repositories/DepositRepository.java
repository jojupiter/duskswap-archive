package com.dusk.duskswap.deposit.repositories;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.deposit.models.Deposit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface DepositRepository extends PagingAndSortingRepository<Deposit, Long> {
    Page<Deposit> findAll(Pageable pageable);
    Page<Deposit> findByExchangeAccount(ExchangeAccount exchangeAccount, Pageable pageable);
    Optional<Deposit> findByInvoiceId(String invoiceId);
}
