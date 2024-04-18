package org.coca.mybatis.config;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {
                MappedStatement.class, Object.class }),
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class }) })
public class MybatisRouterPlugin implements Interceptor {

    protected static final Logger logger = LoggerFactory.getLogger(MybatisRouterPlugin.class);

    private static final String REGEX = ".*insert\\u0020.*|.*delete\\u0020.*|.*update\\u0020.*";

    private static final Map<String, String> cacheMap = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        boolean synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        if (!synchronizationActive && DynamicDataSourceHolder.getDataSource() == null) {

            Object[] objects = invocation.getArgs();
            MappedStatement ms = (MappedStatement) objects[0];

            String key = "";
            if ((key = cacheMap.get(ms.getId())) == null) {
                //read
                if (ms.getSqlCommandType().equals(SqlCommandType.SELECT)) {
                    if (ms.getId().contains(SelectKeyGenerator.SELECT_KEY_SUFFIX)) {
                        key = DataSources.MASTER_DATASOURCE;
                    } else {
                        BoundSql boundSql = ms.getSqlSource().getBoundSql(objects[1]);
                        String sql = boundSql.getSql().toLowerCase(Locale.CHINA).replaceAll("[\\t\\n\\r]", " ");
                        if (sql.matches(REGEX)) {
                            key = DataSources.MASTER_DATASOURCE;
                        } else {
                            key = DataSources.SLAVE_DATASOURCE;
                        }
                    }
                } else {
                    key = DataSources.MASTER_DATASOURCE;
                }
                logger.info("set method[{}] use [{}] Strategy, SqlCommandType [{}]..", ms.getId(), key, ms.getSqlCommandType().name());
                cacheMap.put(ms.getId(), key);
            }
            DynamicDataSourceHolder.putDataSource(key);
//            System.out.println(DynamicDataSourceHolder.getDataSource());
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
