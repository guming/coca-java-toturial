package com.example.utexample.dao;

import com.example.utexample.model.Country;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface CountryMapper {
    Country get(int id);

    void insert(Country country);
}
