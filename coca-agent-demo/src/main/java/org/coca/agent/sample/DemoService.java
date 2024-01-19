package org.coca.agent.sample;

import java.util.List;

public class DemoService {
    public String report(String name, int value) {
        return String.format("name: %s, value: %s", name, value);
    }

    public void compute(List<Integer> values) {
        System.out.println("compute result:" + values.stream().mapToInt(v -> v.intValue()).sum());
    }
}
