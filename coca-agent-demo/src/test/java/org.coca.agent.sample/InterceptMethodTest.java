package org.coca.agent.sample;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class InterceptMethodTest {
    @Test
    public void creatingClassNonName() throws Exception {
        String toString = "hello ByteBuddy";
        DynamicType.Unloaded<Object> unloaded = new ByteBuddy()
                .subclass(Object.class)
                .method(named("toString"))
                .intercept(FixedValue.value(toString))
                .make();

        Class<? extends Object> clazz = unloaded
                .load(InterceptMethodTest.class.getClassLoader())
                .getLoaded();
        Assert.assertEquals(clazz.getDeclaredConstructor().newInstance().toString(), toString);
    }
    @Test
    public void creatingClass(){
        String toString = "hello ByteBuddy";
        String name = "org.coca.agent.sample.ByteBuddyObject";
        DynamicType.Unloaded<Object> unloaded = new ByteBuddy()
                .subclass(Object.class)
                .name("org.coca.agent.sample.ByteBuddyObject")
                .method(named("toString"))
                .intercept(FixedValue.value(toString))
                .make();

        Class<? extends Object> clazz = unloaded
                .load(InterceptMethodTest.class.getClassLoader())
                .getLoaded();
        try {
            Assert.assertEquals(clazz.getDeclaredConstructor().newInstance().toString(), toString);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(clazz.getName(), name);
    }
    @org.junit.Test
    public void delegateMethod() throws Exception {
        DynamicType.Unloaded<People> unloaded = new ByteBuddy()
                .subclass(People.class)
                .name("com.joe.ByteBuddyObject")
                .method(named("say"))
                .intercept(MethodDelegation.withDefaultConfiguration().
                        filter(ElementMatchers.named("sayHi")).
                        to(new Chinese()))
                .make();

        Class<? extends People> clazz = unloaded
                .load(InterceptMethodTest.class.getClassLoader())
                .getLoaded();
        Assert.assertSame(clazz.getInterfaces()[0], People.class);
        Assert.assertEquals(clazz.newInstance().say(), "hi Chinese");
    }

    public interface People{
        String say();
    }

    public class Chinese {
        public String sayHi() {
            return "hi Chinese";
        }
        public String sayHello() {
            return "hello Chinese";
        }
    }



}
