/*
 * Copyright (C) 2017 www.chuchujie.com All rights reserved.
 *
 * Created by xujiangtao@chuchujie.com on 2018/1/11.
 */

package com.example.utexample.config;


import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.example.utexample.dao")
@Slf4j
class MybatisConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        //创建SqlSessionFactoryBean对象
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        //设置数据源
        sessionFactory.setDataSource(dataSource);
        //设置Mapper.xml路径
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/*.xml"));

        return sessionFactory.getObject();
    }


}