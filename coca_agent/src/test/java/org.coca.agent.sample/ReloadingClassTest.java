package org.coca.agent.sample;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import org.junit.Assert;
import org.junit.Test;


public class ReloadingClassTest {
    class Foo {
        String m(){
            return "foo";
        }
    }
    class Bar {
        String m(){
            return "bar";
        }
    }
    @Test
    public void testRedefinition(){
        ByteBuddyAgent.install();
        Foo foo = new Foo();
        Assert.assertEquals(foo.m(), "foo");
        new ByteBuddy()
                .redefine(Bar.class)
                .name(Foo.class.getName())
                .make()
                .load(Foo.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
        Assert.assertEquals(foo.m(), "bar");
    }

}
