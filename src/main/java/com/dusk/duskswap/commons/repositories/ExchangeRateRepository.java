package com.dusk.duskswap.commons.repositories;

import com.dusk.duskswap.commons.models.Conversion;
import org.springframework.data.repository.CrudRepository;

public interface ExchangeRateRepository extends CrudRepository<Conversion, Long> {
}
