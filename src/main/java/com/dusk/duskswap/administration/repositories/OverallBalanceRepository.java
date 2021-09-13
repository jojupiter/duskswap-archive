package com.dusk.duskswap.administration.repositories;

import com.dusk.duskswap.administration.models.OverallBalance;
import com.dusk.duskswap.commons.models.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OverallBalanceRepository extends JpaRepository<OverallBalance, Long> {

    Optional<OverallBalance> findByCurrency(Currency currency);
    Boolean existsByCurrency(Currency currency);

}
