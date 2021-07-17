package com.dusk.duskswap.exchange.repositories;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.exchange.models.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

    List<Exchange> findByExchangeAccount(ExchangeAccount exchangeAccount);

}
