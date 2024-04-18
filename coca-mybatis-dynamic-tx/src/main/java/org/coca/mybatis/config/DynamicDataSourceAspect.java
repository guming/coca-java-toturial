package org.coca.mybatis.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Order(100)
@Component
@Slf4j
public class DynamicDataSourceAspect {
    public static final Logger log = LoggerFactory.getLogger(DynamicDataSourceAspect.class);
    @Before("@annotation(org.coca.mybatis.config.FocusMaster)")
    public void beforeSwitchDS(JoinPoint point){
        Class<?> className = point.getTarget().getClass();
        String methodName = point.getSignature().getName();
        Class[] argClass = ((MethodSignature)point.getSignature()).getParameterTypes();
        String dataSource = DataSources.MASTER_DATASOURCE;//default
        try {
            Method method = className.getMethod(methodName, argClass);
            if (method.isAnnotationPresent(FocusMaster.class)) {
                FocusMaster annotation = method.getAnnotation(FocusMaster.class);
                dataSource = annotation.value();
            }
        } catch (Exception e) {
            log.error("routing datasource exception, " + methodName, e);
        }
        // switch datasource key
        DynamicDataSourceHolder.putDataSource(dataSource);
    }

    @After("@annotation(org.coca.mybatis.config.FocusMaster)")
    public void afterSwitchDS(JoinPoint point){
        DynamicDataSourceHolder.cleanDataSource();
    }
}