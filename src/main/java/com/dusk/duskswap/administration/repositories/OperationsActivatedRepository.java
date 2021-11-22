package com.dusk.duskswap.administration.repositories;

import com.dusk.duskswap.administration.models.OperationsActivated;
import com.dusk.duskswap.commons.models.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OperationsActivatedRepository extends JpaRepository<OperationsActivated, Long> {
    Boolean existsByCurrency(Currency currency);
    Optional<OperationsActivated> findByCurrency(Currency currency);
}
