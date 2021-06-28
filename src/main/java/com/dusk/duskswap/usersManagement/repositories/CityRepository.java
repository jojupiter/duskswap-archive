package com.dusk.duskswap.usersManagement.repositories;

import com.dusk.duskswap.usersManagement.models.City;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CityRepository extends CrudRepository<City, Long> {
    List<City> findAll();
}
