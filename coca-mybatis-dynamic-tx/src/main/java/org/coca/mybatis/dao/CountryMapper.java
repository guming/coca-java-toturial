package org.coca.mybatis.dao;

import org.coca.simple.model.Country;

public interface CountryMapper {
    Country get(int id);

    void insert(Country country);
}
