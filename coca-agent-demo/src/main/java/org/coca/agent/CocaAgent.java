package org.coca.agent;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.coca.agent.core.plugin.DelegateTemplate;
import org.coca.agent.sample.DemoService;
import org.coca.agent.plugin.SimpleInstanceInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;


/**
 * Agent bootstrap
 */
public class CocaAgent {

    public static final Logger LOGGER = LoggerFactory.getLogger(CocaAgent.class);

    public static void main(String[] args) {
        try {
            premain(null,ByteBuddyAgent.install());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DemoService demoService = new DemoService();
        System.out.println(demoService.report("mgu",2 ));
    }
    public static void premain(String arguments, Instrumentation instrumentation) throws IOException {
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Agent starting...");
        }
        //work fine for jdk11+
        //using unsafe injection
        Map<String, byte[]> classesTypeMap = new HashMap<>();
        classesTypeMap.put(new TypeDescription.ForLoadedType(MyInterceptor.class).getName(),
                ClassFileLocator.ForClassLoader.read(MyInterceptor.class));
        ClassInjector.UsingUnsafe.Factory factory = ClassInjector.UsingUnsafe.Factory.resolve(instrumentation);
        factory.make(null, null).injectRaw(classesTypeMap);
        new AgentBuilder.Default()
                .ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
                .with(new AgentBuilder.InjectionStrategy.UsingUnsafe.OfFactory(factory))
                .type(ElementMatchers.nameEndsWith(".DemoService"))
                .transform((AgentBuilder.Transformer) (builder, typeDescription, classLoader, javaModule) ->
                        builder.method(ElementMatchers.named("report"))
                                .intercept(MethodDelegation.to(new DelegateTemplate(new SimpleInstanceInterceptor()))))
                .installOn(instrumentation);
    }

    public static class MyInterceptor {
        @RuntimeType
        public static String intercept(@AllArguments Object[] allArguments,
                                @Origin Method method, @SuperCall Callable<String> callable) throws Exception {
            // intercept any method of any signature
            System.out.println("intercept");
            return callable.call();
        }
    }
}
