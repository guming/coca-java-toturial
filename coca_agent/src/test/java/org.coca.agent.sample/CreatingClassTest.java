package org.coca.agent.sample;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.pool.TypePool;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;

public class CreatingClassTest {
    @Test
    public void redefine() throws NoSuchFieldException {
        TypePool typePool = TypePool.Default.ofSystemLoader();
        Class bar = new ByteBuddy()
                .redefine(typePool.describe("org.coca.agent.sample.Bar").resolve(), // do not use 'Bar.class'
                        ClassFileLocator.ForClassLoader.ofSystemLoader())
                .defineField("qux", String.class) // we learn more about defining fields later
                .make()
                .load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
        Assert.assertNotEquals(bar.getDeclaredField("qux"), null);
    }

}
