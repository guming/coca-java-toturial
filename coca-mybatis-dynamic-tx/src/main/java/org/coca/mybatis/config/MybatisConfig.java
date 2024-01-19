/*
 * Copyright (C) 2017 www.chuchujie.com All rights reserved.
 *
 * Created by xujiangtao@chuchujie.com on 2018/1/11.
 */

package org.coca.mybatis.config;


import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Arrays;
import java.util.List;

@Configuration
@MapperScan(basePackages = {"org.coca.mybatis"},sqlSessionFactoryRef = "sqlSessionFactory")
@Slf4j
class MybatisConfig {

    @Bean(name = "sqlSessionFactory")
    @ConfigurationProperties(prefix = "mybatis")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dynamicDataSource") DynamicDataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setPlugins(new Interceptor[] {new MybatisRouterPlugin()});
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:mybatis/*.xml");
        List<Resource> resourceList = Lists.newArrayList();
        resourceList.addAll(Arrays.asList(resources));
        sqlSessionFactoryBean.setMapperLocations(resourceList.toArray(new Resource[0]));

        return sqlSessionFactoryBean.getObject();
    }
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }


}