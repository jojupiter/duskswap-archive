package com.dusk.duskswap.deposit.repositories;

import com.dusk.duskswap.deposit.models.Deposit;
import org.springframework.data.repository.CrudRepository;

public interface DepositRepository extends CrudRepository<Deposit, Long> {
}
