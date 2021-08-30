package com.dusk.duskswap.deposit.repositories;

import com.dusk.duskswap.deposit.models.DepositHash;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface DepositHashRepository extends PagingAndSortingRepository<DepositHash, Long> {

    Page<DepositHash> findAll(Pageable pageable);
    @Query(value = "SELECT ", nativeQuery = true) // TODO: do this query
    Page<DepositHash> findByExchangeAccount(Pageable pageable);

    Boolean existsByTransactionHash(String transactionHash);
    Optional<DepositHash> findByTransactionHash(String transactionHash);

    @Query(value = "SELECT COUNT(*) FROM deposit_hash WHERE deposit_id = ?1", nativeQuery = true)
    Long countTotalDepositHash(Long depositId);

}
