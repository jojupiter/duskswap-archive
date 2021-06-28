package com.dusk.duskswap.usersManagement.repositories;

import com.dusk.duskswap.usersManagement.models.Country;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CountryRepository extends CrudRepository<Country, Long> {
    List<Country> findAll();
}
