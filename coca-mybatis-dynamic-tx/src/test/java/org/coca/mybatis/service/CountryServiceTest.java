package org.coca.mybatis.service;

import org.coca.simple.model.Country;
import org.coca.mybatis.ApplicationTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CountryServiceTest extends ApplicationTest {
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
