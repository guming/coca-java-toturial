package coca.ratelimiter.internal;

import coca.core.RegistryStore;
import coca.core.registry.AbstractRegistry;
import coca.core.registry.InMemoryRegistryStore;
import coca.core.registry.RegistryEventConsumer;
import coca.ratelimiter.RateLimiter;
import coca.ratelimiter.RateLimiterConfig;
import coca.ratelimiter.RateLimiterRegistry;

import java.util.*;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;

public class InMemoryRateLimiterRegistry extends AbstractRegistry<RateLimiter,RateLimiterConfig> implements RateLimiterRegistry {

    public InMemoryRateLimiterRegistry() {
        this(RateLimiterConfig.ofDefaults());
    }

    public InMemoryRateLimiterRegistry(Map<String, RateLimiterConfig> configs) {
        this(configs, emptyMap());
    }

    public InMemoryRateLimiterRegistry(Map<String, RateLimiterConfig> configs, Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, RateLimiterConfig.ofDefaults()), tags);
        this.configurations.putAll(configs);
    }

    public InMemoryRateLimiterRegistry(Map<String, RateLimiterConfig> configs,
                                       RegistryEventConsumer<RateLimiter> registryEventConsumer) {
        this(configs, registryEventConsumer, emptyMap());
    }

    public InMemoryRateLimiterRegistry(Map<String, RateLimiterConfig> configs,
                                       RegistryEventConsumer<RateLimiter> registryEventConsumer, Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, RateLimiterConfig.ofDefaults()),
                registryEventConsumer, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryRateLimiterRegistry(Map<String, RateLimiterConfig> configs,
                                       List<RegistryEventConsumer<RateLimiter>> registryEventConsumers) {
        this(configs, registryEventConsumers, emptyMap());
    }

    public InMemoryRateLimiterRegistry(Map<String, RateLimiterConfig> configs,
                                       List<RegistryEventConsumer<RateLimiter>> registryEventConsumers, Map<String, String> tags) {
        this(configs.getOrDefault(DEFAULT_CONFIG, RateLimiterConfig.ofDefaults()),
                registryEventConsumers, tags);
        this.configurations.putAll(configs);
    }

    public InMemoryRateLimiterRegistry(Map<String, RateLimiterConfig> configs, List<RegistryEventConsumer<RateLimiter>> registryEventConsumers, Map<String, String> tags,
                                       RegistryStore<RateLimiter> registryStore) {
        super(configs.getOrDefault(DEFAULT_CONFIG, RateLimiterConfig.ofDefaults()),
                registryEventConsumers, Optional.ofNullable(tags).orElse(emptyMap()),
                Optional.ofNullable(registryStore).orElse(new InMemoryRegistryStore<>()));
        this.configurations.putAll(configs);
    }


    /**
     * default config
     * @param defaultConfig
     */
    public InMemoryRateLimiterRegistry(RateLimiterConfig defaultConfig) {
        super(defaultConfig);
    }

    public InMemoryRateLimiterRegistry(RateLimiterConfig defaultConfig, Map<String, String> registryTags) {
        super(defaultConfig, registryTags);
    }

    public InMemoryRateLimiterRegistry(RateLimiterConfig defaultConfig, RegistryEventConsumer<RateLimiter> registryEventConsumer) {
        super(defaultConfig, registryEventConsumer);
    }

    public InMemoryRateLimiterRegistry(RateLimiterConfig defaultConfig, RegistryEventConsumer<RateLimiter> registryEventConsumer, Map<String, String> tags) {
        super(defaultConfig, registryEventConsumer, tags);
    }

    public InMemoryRateLimiterRegistry(RateLimiterConfig defaultConfig, List<RegistryEventConsumer<RateLimiter>> registryEventConsumers) {
        super(defaultConfig, registryEventConsumers);
    }

    public InMemoryRateLimiterRegistry(RateLimiterConfig defaultConfig, List<RegistryEventConsumer<RateLimiter>> registryEventConsumers, Map<String, String> tags) {
        super(defaultConfig, registryEventConsumers, tags);
    }

    public InMemoryRateLimiterRegistry(RateLimiterConfig defaultConfig, List<RegistryEventConsumer<RateLimiter>> registryEventConsumers, Map<String, String> tags, RegistryStore<RateLimiter> registryStore) {
        super(defaultConfig, registryEventConsumers, tags, registryStore);
    }


    @Override
    public Set<RateLimiter> getAllRateLimiters() {
        return new HashSet<>(entryMap.values());
    }

    @Override
    public RateLimiter rateLimiter(String name) {
        return rateLimiter(name,getDefaultConfig());
    }

    @Override
    public RateLimiter rateLimiter(String name, Map<String, String> tags) {
        return rateLimiter(name,getDefaultConfig(),tags);
    }

    @Override
    public RateLimiter rateLimiter(String name, RateLimiterConfig rateLimiterConfig) {
        return rateLimiter(name,rateLimiterConfig,Collections.emptyMap());
    }

    @Override
    public RateLimiter rateLimiter(String name, RateLimiterConfig rateLimiterConfig, Map<String, String> tags) {
        return null;
    }

    @Override
    public RateLimiter rateLimiter(String name, Supplier<RateLimiterConfig> rateLimiterConfigSupplier) {
        return rateLimiter(name, rateLimiterConfigSupplier, emptyMap());
    }

    @Override
    public RateLimiter rateLimiter(String name, Supplier<RateLimiterConfig> rateLimiterConfigSupplier, Map<String, String> tags) {
        return null;
    }

    @Override
    public RateLimiter rateLimiter(String name, String configName) {
        return rateLimiter(name,configName);
    }

    @Override
    public RateLimiter rateLimiter(String name, String configName, Map<String, String> tags) {
        return null;
    }
}
