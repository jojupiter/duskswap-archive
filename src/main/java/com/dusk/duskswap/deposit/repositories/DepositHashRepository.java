package com.dusk.duskswap.deposit.repositories;

import com.dusk.duskswap.deposit.models.DepositHash;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface DepositHashRepository extends PagingAndSortingRepository<DepositHash, Long> {

    Page<DepositHash> findAll(Pageable pageable);
    @Query(value = "SELECT * FROM deposit_hash JOIN (SELECT * FROM deposit WHERE exchange_account_id = ?1) AS depuser" +
            " ON deposit_hash.deposit_id = depuser.id ORDER BY deposit_hash.last_update DESC;", nativeQuery = true)
    Page<DepositHash> findByExchangeAccount(Long exchangeAccountId, Pageable pageable);

    Boolean existsByTransactionHash(String transactionHash);
    Optional<DepositHash> findByTransactionHash(String transactionHash);

    @Query(value = "SELECT COUNT(*) FROM deposit_hash WHERE deposit_id = ?1", nativeQuery = true)
    Long countTotalDepositHash(Long depositId);

}
