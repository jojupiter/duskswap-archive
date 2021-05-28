package com.dusk.shared.usersManagement.repositories;

import com.dusk.shared.usersManagement.models.Enterprise;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface EnterpriseRepository extends CrudRepository<Enterprise, Long> {
    List<Enterprise> findAll();
    @Query(value = "SELECT * FROM enterprise WHERE owner_id = ?1", nativeQuery = true)
    Optional<Enterprise> findByOwnerId(Long owner_id);
}
