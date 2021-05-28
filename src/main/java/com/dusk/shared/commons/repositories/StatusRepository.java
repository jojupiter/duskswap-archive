package com.dusk.shared.commons.repositories;

import com.dusk.shared.commons.models.Status;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface StatusRepository extends CrudRepository<Status, Long> {

    Optional<Status> findByName(String name);
    List<Status> findAll();
}
