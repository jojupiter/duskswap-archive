package com.dusk.shared.usersManagement.repositories;

import com.dusk.shared.usersManagement.models.City;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CityRepository extends CrudRepository<City, Long> {
    List<City> findAll();
}
