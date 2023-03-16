package com.example.utexample.api;

import com.example.utexample.model.Country;
import com.example.utexample.service.CountryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping(path = "/country")
public class RestApi {

    @Autowired
    CountryServiceImpl countryServiceImpl;
    @GetMapping("/{id}")
    public Country getCountry(@PathVariable String id) {
        Country country = countryServiceImpl.getCountry(Integer.valueOf(id));
        System.out.println(country);
        return country;
    }
}
