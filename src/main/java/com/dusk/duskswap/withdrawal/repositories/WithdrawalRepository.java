package com.dusk.duskswap.withdrawal.repositories;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.withdrawal.entityDto.WithdrawalProfit;
import com.dusk.duskswap.withdrawal.models.Withdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

public interface WithdrawalRepository extends PagingAndSortingRepository<Withdrawal, Long> {
    Page<Withdrawal> findAll(Pageable pageable);
    Page<Withdrawal> findByExchangeAccount(ExchangeAccount exchangeAccount, Pageable pageable);

    @Query(value = "SELECT currency, SUM(CAST(dusk_fees_crypto AS DOUBLE PRECISION)) AS sum" +
            "FROM withdrawal GROUP BY currency_id", nativeQuery = true)
    List<WithdrawalProfit> findAllProfits();
    //@Query(value = "")
    //List<WithdrawalProfit> findAllProfitsAfter(Date date);
    //@Query(value = "")
    //List<WithdrawalProfit> findAllProfitsBefore(Date date);
    //@Query(value = "")
    //List<WithdrawalProfit> findAllProfitsBetween(Date start, Date end);
}
