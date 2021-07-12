package com.dusk.duskswap.commons.repositories;

import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Level;
import com.dusk.duskswap.commons.models.Pricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PricingRepository extends JpaRepository<Pricing, Long> {

    @Query(value = "SELECT * FROM pricing GROUP BY level_id", nativeQuery = true)
    List<Pricing> findAllGroupByLevel();

    List<Pricing> findByLevel(Level level);

    List<Pricing> findByCurrency(Currency currency);

}
