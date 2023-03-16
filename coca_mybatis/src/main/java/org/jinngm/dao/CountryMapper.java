package org.jinngm.dao;

import mybatis.simple.model.Country;

public interface CountryMapper {
    Country get(int id);

    void insert(Country country);
}
