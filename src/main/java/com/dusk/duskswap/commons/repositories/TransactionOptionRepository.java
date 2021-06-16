package com.dusk.duskswap.commons.repositories;

import com.dusk.duskswap.commons.models.TransactionOption;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionOptionRepository extends CrudRepository<TransactionOption, Long> {
    Optional<TransactionOption> findByName(String name);
    List<TransactionOption> findByIsSupported(Boolean isSupported);
}
