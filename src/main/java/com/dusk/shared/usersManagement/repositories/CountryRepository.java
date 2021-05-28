package com.dusk.shared.usersManagement.repositories;

import com.dusk.shared.usersManagement.models.Country;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CountryRepository extends CrudRepository<Country, Long> {
    List<Country> findAll();
}
