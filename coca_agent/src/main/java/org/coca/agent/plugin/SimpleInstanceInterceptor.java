package org.coca.agent.plugin;

import org.coca.agent.core.plugin.MethodAroundInterceptor;

import java.lang.reflect.Method;

public class SimpleInstanceInterceptor implements MethodAroundInterceptor {
    @Override
    public void before(Object inst, Method interceptPoint, Object[] allArguments, Class<?>[] argumentsTypes) {
        System.out.println("Interceptor in ..."+System.nanoTime());
    }

    @Override
    public Object after(Object inst, Method interceptPoint, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) {
        System.out.println("Interceptor out ..."+System.nanoTime());
        return ret;
    }

    @Override
    public void handleException(Object inst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        System.out.println("Interceptor error handle ...");
    }
}
