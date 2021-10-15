package com.dusk.duskswap.withdrawal.repositories;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.withdrawal.models.Sell;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface SellRepository extends PagingAndSortingRepository<Sell, Long> {

    Page<Sell> findAll(Pageable pageable);
    List<Sell> findAll();
    Page<Sell> findByExchangeAccount(ExchangeAccount exchangeAccount, Pageable pageable);
    List<Sell> findBySellDateAfter(Date date);
    List<Sell> findBySellDateBefore(Date date);
    List<Sell> findBySellDateBetween(Date startDate, Date endDate);
    Optional<Sell> findByTransactionId(String transactionId);

}
