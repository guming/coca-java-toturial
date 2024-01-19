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

        //获得当前访问的class
        Class<?> className = point.getTarget().getClass();

        //获得访问的方法名
        String methodName = point.getSignature().getName();
        //得到方法的参数的类型
        Class[] argClass = ((MethodSignature)point.getSignature()).getParameterTypes();
        String dataSource = DataSources.MASTER_DATASOURCE;//default
        try {
            // 得到访问的方法对象
            Method method = className.getMethod(methodName, argClass);

            // 判断是否存在@DS注解
            if (method.isAnnotationPresent(FocusMaster.class)) {
                FocusMaster annotation = method.getAnnotation(FocusMaster.class);
                // 取出注解中的数据源名
                dataSource = annotation.value();
            }
        } catch (Exception e) {
            log.error("routing datasource exception, " + methodName, e);
        }
        // 切换数据源
        DynamicDataSourceHolder.putDataSource(dataSource);
    }

    @After("@annotation(org.coca.mybatis.config.FocusMaster)")
    public void afterSwitchDS(JoinPoint point){
        DynamicDataSourceHolder.cleanDataSource();
    }
}