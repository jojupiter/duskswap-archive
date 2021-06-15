package com.dusk.duskswap.deposit.repositories;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.deposit.models.Deposit;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DepositRepository extends CrudRepository<Deposit, Long> {
    List<Deposit> findAll();
    List<Deposit> findByExchangeAccount(ExchangeAccount exchangeAccount);
    Optional<Deposit> findByInvoiceId(String invoiceId);
}
