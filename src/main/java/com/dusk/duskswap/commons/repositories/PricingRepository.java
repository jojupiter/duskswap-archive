package com.dusk.duskswap.commons.repositories;

import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Level;
import com.dusk.duskswap.commons.models.Pricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PricingRepository extends JpaRepository<Pricing, Long> {

    List<Pricing> findByLevel(Level level);

    List<Pricing> findByCurrency(Currency currency);

    Optional<Pricing> findByLevelAndCurrency(Level level, Currency currency);

    Boolean existsByLevelAndCurrency(Level level, Currency currency);

}
