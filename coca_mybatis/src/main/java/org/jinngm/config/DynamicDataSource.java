package org.jinngm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DynamicDataSource extends AbstractRoutingDataSource {

    public static final Logger logger = LoggerFactory.getLogger(DynamicDataSource.class);

    private List<Object> slaveDataSources=new ArrayList<Object>();

    private AtomicInteger squence = new AtomicInteger(0);

    public void setSlaveDataSources(List<Object> slaveDataSources) {
        this.slaveDataSources = slaveDataSources;
    }

//    @Override
//    public void afterPropertiesSet() {
//        if (this.masterDataSource == null) {
//            throw new IllegalArgumentException("Property 'writeDataSource' is required");
//        }
//        setDefaultTargetDataSource(masterDataSource);
//        Map<Object, Object> targetDataSources = new HashMap<>();
//        targetDataSources.put(DataSources.MASTER_DATASOURCE, masterDataSource);
//        if (slaveDataSources != null) {
//            targetDataSources.put(DynamicDataSourceGlobal.READ.name(), readDataSource);
//        }
//        setTargetDataSources(targetDataSources);
//        super.afterPropertiesSet();
//    }

    @Override
    protected Object determineCurrentLookupKey() {
        Object key = "";
        if(DynamicDataSourceHolder.isMaster()){
            key = DataSources.MASTER_DATASOURCE;
            logger.info("choose master");
        }else {
            key = getSlaveKey();
            logger.info("choose slave");
        }
        //clean
        DynamicDataSourceHolder.cleanDataSource();
        return key;
    }

    public Object getSlaveKey() {
        if (squence.intValue() == Integer.MAX_VALUE) {
            synchronized (squence) {
                if (squence.intValue() == Integer.MAX_VALUE) {
                    squence = new AtomicInteger(0);
                }
            }
        }
        int idx = squence.getAndIncrement() % slaveDataSources.size();
        return slaveDataSources.get(idx);
    }
}
