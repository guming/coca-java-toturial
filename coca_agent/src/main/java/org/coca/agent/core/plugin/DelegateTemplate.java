package org.coca.agent.core.plugin;

import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class DelegateTemplate {
    private MethodAroundInterceptor interceptor;

    public DelegateTemplate(MethodAroundInterceptor interceptor) {
        this.interceptor = interceptor;
    }
    @RuntimeType
    public Object intercept(@This Object instrument, @AllArguments Object[] allArguments,
                            @SuperCall Callable<?> zuper, @Origin Method method){
        if(interceptor == null){
            return null;
        }
        Object result = null;
        interceptor.before(instrument, method, allArguments, method.getParameterTypes());
        try {
            result = zuper.call();
            interceptor.after(instrument, method, allArguments, method.getParameterTypes(), result);
        } catch (Exception e) {
            interceptor.handleException(instrument,method,allArguments, method.getParameterTypes(), e);
        }
        return result;
    }

}
