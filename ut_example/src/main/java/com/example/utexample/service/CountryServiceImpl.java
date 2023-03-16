package com.example.utexample.service;

import com.example.utexample.model.Country;
import com.example.utexample.dao.CountryMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CountryServiceImpl {
    @Resource
    private CountryMapper countryMapper;
    //    @Transactional(value = "transactionManager",readOnly=true)
//    @FocusMaster
    public Country getCountry(int id){
        Country country = countryMapper.get(id);
        return country;
    }
    //    @Transactional(rollbackFor = RuntimeException.class, propagation = Propagation.REQUIRED,value = "transactionManager")
    public void add(){
        Country country1 = countryMapper.get(1);
        System.out.println(country1.toString());
        Country country = new Country();
        country.setCountrycode("test");
        country.setCountryname("test2");
        countryMapper.insert(country);
    }
}
