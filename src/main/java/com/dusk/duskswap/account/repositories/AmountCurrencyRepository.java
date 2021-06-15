package com.dusk.duskswap.account.repositories;

import com.dusk.duskswap.account.models.AmountCurrency;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.shared.commons.models.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AmountCurrencyRepository extends JpaRepository<AmountCurrency, Long> {

    @Query(value = "SELECT * FROM amount_currency WHERE exchange_account_id = ?1", nativeQuery = true)
    List<AmountCurrency> findByAccountId(Long accountId);

    @Query(value = "SELECT * FROM amount_currency WHERE exchange_account_id = ?1 AND currency_id = ?2", nativeQuery = true)
    Optional<AmountCurrency> findByAccountAndCurrencyId(Long accountId, Long currencyId);

}
