package org.coca.agent.core.plugin;

import java.lang.reflect.Method;

public interface MethodAroundInterceptor {
    void before(Object inst, Method interceptPoint,
                Object[] allArguments, Class<?>[] argumentsTypes);
    Object after(Object inst, Method interceptPoint,
                       Object[] allArguments, Class<?>[] argumentsTypes,
                       Object ret);

    void handleException(Object inst, Method method, Object[] allArguments,
                               Class<?>[] argumentsTypes, Throwable t);
}
