package com.dusk.shared.usersManagement.services;

import com.dusk.shared.usersManagement.models.City;
import com.dusk.shared.usersManagement.models.Country;
import com.dusk.shared.usersManagement.repositories.CityRepository;
import com.dusk.shared.usersManagement.repositories.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private CountryRepository countryRepository;

    // =========================== LOCATION SERVICES ================================
    // this includes city and country
    //cities
    @Override
    public City getCityById(Long id) {
        if(id != null) {
            return cityRepository.findById(id).get();
        }
        return null;
    }
    @Override
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }
    @Override
    public City addCity(City city) {
        if(city != null)
            return cityRepository.save(city);
        return null;
    }
    @Override
    public City updateCity(Long id, City newCity){
        if(id == null || newCity == null)
            return null;
        newCity.setId(id);
        return cityRepository.save(newCity);
    }
    @Override
    public void deleteCity(Long id) {
        if(id != null)
            cityRepository.deleteById(id);
    }

    //countries
    @Override
    public Country getCountryById(Long id) {
        if(id != null)
            return countryRepository.findById(id).get();
        return null;
    }
    @Override
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }
    @Override
    public Country addCountry(Country country) {
        if(country != null)
            return countryRepository.save(country);
        return null;
    }
    @Override
    public Country updateCountry(Long id, Country newCountry){
        if(id == null || newCountry == null)
            return null;
        newCountry.setId(id);
        return countryRepository.save(newCountry);
    }
    @Override
    public void deleteCountry(Long id) {
        if(id != null)
            cityRepository.deleteById(id);
    }


}
