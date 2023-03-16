package org.coca.agent.sample;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class ToStringAgent {
    public static void premain(String arguments, Instrumentation instrumentation) {

        new AgentBuilder.Default()
                .type(isAnnotatedWith(ToString.class))
                .transform((builder, typeDescription, classLoader, javaModule) -> builder.method(named("toString"))
                        .intercept(FixedValue.value("transformed"))).installOn(instrumentation);
    }
}
