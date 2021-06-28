package com.dusk.duskswap.withdrawal.repositories;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.withdrawal.models.Sell;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SellRepository extends CrudRepository<Sell, Long> {

    List<Sell> findAll();
    List<Sell> findByExchangeAccount(ExchangeAccount exchangeAccount);

}
