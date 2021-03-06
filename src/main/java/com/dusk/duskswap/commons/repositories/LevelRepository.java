package com.dusk.duskswap.commons.repositories;

import com.dusk.duskswap.commons.models.Level;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface LevelRepository extends CrudRepository<Level, Long> {

    Optional<Level> findByName(String name);
    Optional<Level> findByIso(String iso);
    List<Level> findAll();
}
