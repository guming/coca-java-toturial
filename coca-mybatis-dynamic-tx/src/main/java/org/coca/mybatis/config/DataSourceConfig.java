package org.coca.mybatis.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * datasource
 */
@Configuration
@Slf4j
class DataSourceConfig {

    @Bean(initMethod = "init", destroyMethod = "close", name = DataSources.MASTER_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.druid.master")
    public DruidDataSource master() throws SQLException {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(initMethod = "init", destroyMethod = "close", name = DataSources.SLAVE_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.druid.slave")
    public DruidDataSource slave() throws SQLException {
        return DruidDataSourceBuilder.create().build();
    }
    @Bean(name = "transactionManager")
    @Primary
    public DataSourceTransactionManager transactionManager(@Qualifier("dynamicDataSource") DynamicDataSource dynamicDataSource) throws SQLException {
        DynamicDataSourceTransactionManager dynamicDataSourceTransactionManager = new DynamicDataSourceTransactionManager();
        dynamicDataSourceTransactionManager.setDataSource(dynamicDataSource);
        return dynamicDataSourceTransactionManager;
    }

    @Bean
    public DynamicDataSource dynamicDataSource(@Qualifier("master") DataSource masterDataSource,
                                               @Qualifier("slave") DataSource slaveDataSource){
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> targetDataSources = new HashMap<Object, Object>();

        targetDataSources.put("master", masterDataSource);
        targetDataSources.put("slave1", slaveDataSource);
        dynamicDataSource.setTargetDataSources(targetDataSources);

        List<Object> slaveDataSources = new ArrayList<Object>();
        slaveDataSources.add("slave1");

        dynamicDataSource.setDefaultTargetDataSource(masterDataSource);
        dynamicDataSource.setSlaveDataSources(slaveDataSources);

        return dynamicDataSource;
    }


}
