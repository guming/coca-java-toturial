package com.example.utexample;

import com.example.utexample.model.Country;
import com.example.utexample.service.CountryServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest
@RunWith(SpringRunner.class)

@ActiveProfiles("test")

public class CountryTest {
    @Autowired
    private CountryServiceImpl countryService;

    @Test
    public void getTest(){
        int id=1;
        Country country = countryService.getCountry(id);
        Assert.assertTrue(country.getId()==id);

    }
    @Test
    public void addTest(){
        countryService.add();
    }
}
