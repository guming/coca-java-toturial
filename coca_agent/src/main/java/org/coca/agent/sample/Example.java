package org.coca.agent.sample;

@ToString
public class Example {
    public int age;
    public String name;

    @Override
    public String toString() {
        return "Example{" +
                "age=" + age +
                ", name='" + name + '\'' +
                '}';
    }
}
