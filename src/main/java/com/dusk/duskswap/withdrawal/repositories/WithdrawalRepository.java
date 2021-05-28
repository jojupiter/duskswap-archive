package com.dusk.duskswap.withdrawal.repositories;

import com.dusk.duskswap.withdrawal.models.Withdrawal;
import org.springframework.data.repository.CrudRepository;

public interface WithdrawalRepository extends CrudRepository<Withdrawal, Long> {
}
