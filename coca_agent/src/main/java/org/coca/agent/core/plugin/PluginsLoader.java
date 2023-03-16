package org.coca.agent.core.plugin;

import java.util.List;

public interface PluginsLoader {
    public <T> List<T> load(Class<T> serviceClass);
}
