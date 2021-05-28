package com.dusk.duskswap.account.repositories;

import com.dusk.duskswap.account.models.ExchangeAccount;
import org.springframework.data.repository.CrudRepository;

public interface ExchangeAccountRepository extends CrudRepository<ExchangeAccount, Long> {
}
