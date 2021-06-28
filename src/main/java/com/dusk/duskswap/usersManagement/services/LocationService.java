package com.dusk.duskswap.usersManagement.services;


import com.dusk.duskswap.usersManagement.models.City;
import com.dusk.duskswap.usersManagement.models.Country;

import javax.validation.Valid;
import java.util.List;

public interface LocationService {
    //cities
    public City getCityById(Long id);
    public List<City> getAllCities();
    public City addCity(@Valid City city);
    public City updateCity(Long id, @Valid City newCity);
    public void deleteCity(Long id);

    //countries
    public Country getCountryById(Long id);
    public List<Country> getAllCountries();
    public Country addCountry(@Valid Country country);
    public Country updateCountry(Long id, @Valid Country newCountry);
    public void deleteCountry(Long id);
}
