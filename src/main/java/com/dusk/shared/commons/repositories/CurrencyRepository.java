package com.dusk.shared.commons.repositories;

import com.dusk.shared.commons.models.Currency;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends CrudRepository<Currency, Long> {
    List<Currency> findAll();
    List<Currency> findByIsoIn(Collection<String> isos);
    Optional<Currency> findByIso(String iso);
    List<Currency> findByIsSupported(Boolean isSupported);
}
