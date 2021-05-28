package com.dusk.shared.commons.repositories;

import com.dusk.shared.commons.models.Level;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface LevelRepository extends CrudRepository<Level, Long> {

    Optional<Level> findByName(String name);
    List<Level> findAll();
}
