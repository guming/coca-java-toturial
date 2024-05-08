package coca.bulkhead.internal;

import coca.bulkhead.ThreadPoolBulkhead;
import coca.bulkhead.ThreadPoolBulkheadConfig;
import coca.bulkhead.ThreadPoolBulkheadRegistry;
import coca.core.RegistryStore;
import coca.core.registry.AbstractRegistry;
import coca.core.registry.InMemoryRegistryStore;
import coca.core.registry.RegistryEventConsumer;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;

public class InMemoryThreadPoolBulkheadRegistry extends AbstractRegistry<ThreadPoolBulkhead, ThreadPoolBulkheadConfig> implements ThreadPoolBulkheadRegistry {

    public InMemoryThreadPoolBulkheadRegistry() {
        this(emptyMap());
    }

    public InMemoryThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs) {
        this(configs, emptyMap());
    }

    public InMemoryThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs, Map<String, String> registryTags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, ThreadPoolBulkheadConfig.ofDefaults()), registryTags);
        this.configurations.putAll(configs);
    }
    public InMemoryThreadPoolBulkheadRegistry(
            Map<String, ThreadPoolBulkheadConfig> configs,
            RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer) {
        this(configs, registryEventConsumer, emptyMap());
    }

    public InMemoryThreadPoolBulkheadRegistry(
            Map<String, ThreadPoolBulkheadConfig> configs,
            RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer, Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, ThreadPoolBulkheadConfig.ofDefaults()),
                registryEventConsumer, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryThreadPoolBulkheadRegistry(
            Map<String, ThreadPoolBulkheadConfig> configs,
            List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers) {
        this(configs, registryEventConsumers, emptyMap());
    }

    public InMemoryThreadPoolBulkheadRegistry(
            Map<String, ThreadPoolBulkheadConfig> configs,
            List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers, Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, ThreadPoolBulkheadConfig.ofDefaults()),
                registryEventConsumers, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryThreadPoolBulkheadRegistry(ThreadPoolBulkheadConfig defaultConfig) {
        super(defaultConfig);
    }

    public InMemoryThreadPoolBulkheadRegistry(ThreadPoolBulkheadConfig defaultConfig, Map<String, String> tags) {
        super(defaultConfig, tags);
    }

    public InMemoryThreadPoolBulkheadRegistry(
            ThreadPoolBulkheadConfig defaultConfig,
            RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer) {
        super(defaultConfig, registryEventConsumer);
    }

    public InMemoryThreadPoolBulkheadRegistry(
            ThreadPoolBulkheadConfig defaultConfig,
            RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer, Map<String, String> tags) {
        super(defaultConfig, registryEventConsumer, tags);
    }

    public InMemoryThreadPoolBulkheadRegistry(
            ThreadPoolBulkheadConfig defaultConfig,
            List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers) {
        super(defaultConfig, registryEventConsumers);
    }

    public InMemoryThreadPoolBulkheadRegistry(
            ThreadPoolBulkheadConfig defaultConfig,
            List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers, Map<String, String> tags) {
        super(defaultConfig, registryEventConsumers, tags);
    }

    public InMemoryThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs,
                                              List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers,
                                              Map<String, String> tags, RegistryStore<ThreadPoolBulkhead> registryStore) {
        super(configs.getOrDefault(DEFAULT_CONFIG, ThreadPoolBulkheadConfig.ofDefaults()),
                registryEventConsumers, Optional.ofNullable(tags).orElse(emptyMap()),
                Optional.ofNullable(registryStore).orElse(new InMemoryRegistryStore<>()));
        this.configurations.putAll(configs);
    }

    @Override
    public Set<ThreadPoolBulkhead> getAllBulkheads() {
        return new HashSet<>(entryMap.values());
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name) {
        return bulkhead(name,emptyMap());
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, Map<String, String> tags) {
        return bulkhead(name, getDefaultConfig(), tags);
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, ThreadPoolBulkheadConfig config) {
        return bulkhead(name, config, emptyMap());
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, ThreadPoolBulkheadConfig config, Map<String, String> tags) {
        return computeIfAbsent(name, () -> ThreadPoolBulkhead
                .of(name, Objects.requireNonNull(config, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, Supplier<ThreadPoolBulkheadConfig> bulkheadConfigSupplier) {
        return null;
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, Supplier<ThreadPoolBulkheadConfig> bulkheadConfigSupplier, Map<String, String> tags) {
        return null;
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, String configName) {
        return null;
    }

    @Override
    public ThreadPoolBulkhead bulkhead(String name, String configName, Map<String, String> tags) {
        return null;
    }

    @Override
    public void close() throws IOException {
        for (ThreadPoolBulkhead bulkhead: getAllBulkheads()) {
            try {
                bulkhead.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
