package com.dusk.duskswap.transferring.repositories;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.transferring.models.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TransferRepository extends PagingAndSortingRepository<Transfer, Long> {

    Page<Transfer> findAll(Pageable pageable);
    Page<Transfer> findByFromAccount(ExchangeAccount fromAccount, Pageable pageable);

}
