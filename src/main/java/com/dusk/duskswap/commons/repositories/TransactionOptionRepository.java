package com.dusk.duskswap.commons.repositories;

import com.dusk.duskswap.commons.models.TransactionOption;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TransactionOptionRepository extends CrudRepository<TransactionOption, Long> {
    Optional<TransactionOption> findByName(String name);
}
