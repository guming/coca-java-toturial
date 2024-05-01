package coca.bulkhead;

import coca.core.Registry;
import coca.core.RegistryStore;
import coca.core.registry.RegistryEventConsumer;
import coca.bulkhead.internal.InMemoryThreadPoolBulkheadRegistry;

import java.io.Closeable;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;

public interface ThreadPoolBulkheadRegistry extends Registry<ThreadPoolBulkhead, ThreadPoolBulkheadConfig>, Closeable {

    static ThreadPoolBulkheadRegistry of(ThreadPoolBulkheadConfig bulkheadConfig) {
        return new InMemoryThreadPoolBulkheadRegistry(bulkheadConfig);
    }

    static ThreadPoolBulkheadRegistry of(ThreadPoolBulkheadConfig bulkheadConfig,
                                         RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer) {
        return new InMemoryThreadPoolBulkheadRegistry(bulkheadConfig, registryEventConsumer);
    }

    static ThreadPoolBulkheadRegistry of(ThreadPoolBulkheadConfig bulkheadConfig,
                                         List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers) {
        return new InMemoryThreadPoolBulkheadRegistry(bulkheadConfig, registryEventConsumers);
    }

    static ThreadPoolBulkheadRegistry ofDefaults() {
        return ofDefaults(emptyMap());
    }

    static ThreadPoolBulkheadRegistry ofDefaults(Map<String, String> tags) {
        return new InMemoryThreadPoolBulkheadRegistry(ThreadPoolBulkheadConfig.ofDefaults(), tags);
    }

    static ThreadPoolBulkheadRegistry of(Map<String, ThreadPoolBulkheadConfig> configs) {
        return of(configs, emptyMap());
    }

    static ThreadPoolBulkheadRegistry of(Map<String, ThreadPoolBulkheadConfig> configs, Map<String, String> tags) {
        return new InMemoryThreadPoolBulkheadRegistry(configs, tags);
    }

    static ThreadPoolBulkheadRegistry of(Map<String, ThreadPoolBulkheadConfig> configs,
                                         RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer) {
        return of(configs, registryEventConsumer, emptyMap());
    }

    static ThreadPoolBulkheadRegistry of(Map<String, ThreadPoolBulkheadConfig> configs,
                                         RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer, Map<String, String> tags) {
        return new InMemoryThreadPoolBulkheadRegistry(configs, registryEventConsumer, tags);
    }

    static ThreadPoolBulkheadRegistry of(Map<String, ThreadPoolBulkheadConfig> configs,
                                         List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers) {
        return of(configs, registryEventConsumers, emptyMap());
    }

    static ThreadPoolBulkheadRegistry of(Map<String, ThreadPoolBulkheadConfig> configs,
                                         List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers, Map<String, String> tags) {
        return new InMemoryThreadPoolBulkheadRegistry(configs, registryEventConsumers, tags);
    }

    Set<ThreadPoolBulkhead> getAllBulkheads();

    ThreadPoolBulkhead bulkhead(String name);

    ThreadPoolBulkhead bulkhead(String name, Map<String, String> tags);

    ThreadPoolBulkhead bulkhead(String name, ThreadPoolBulkheadConfig config);

    ThreadPoolBulkhead bulkhead(String name, ThreadPoolBulkheadConfig config, Map<String, String> tags);

    ThreadPoolBulkhead bulkhead(String name,
                                Supplier<ThreadPoolBulkheadConfig> bulkheadConfigSupplier);


    ThreadPoolBulkhead bulkhead(String name,
                                Supplier<ThreadPoolBulkheadConfig> bulkheadConfigSupplier, Map<String, String> tags);

    ThreadPoolBulkhead bulkhead(String name, String configName);

    ThreadPoolBulkhead bulkhead(String name, String configName, Map<String, String> tags);

    class Builder {
        private static final String DEFAULT_CONFIG = "default";
        private RegistryStore<ThreadPoolBulkhead> registryStore;
        private Map<String, ThreadPoolBulkheadConfig> threadPoolBulkheadConfigsMap;
        private List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers;
        private Map<String, String> tags;
        public Builder() {
            this.threadPoolBulkheadConfigsMap = new HashMap<>();
            this.registryEventConsumers = new ArrayList<>();
        }
        public Builder withRegistryStore(RegistryStore<ThreadPoolBulkhead> registryStore) {
            this.registryStore = registryStore;
            return this;
        }
        public Builder withThreadPoolBulkheadConfig(ThreadPoolBulkheadConfig threadPoolBulkheadConfig) {
            threadPoolBulkheadConfigsMap.put(DEFAULT_CONFIG, threadPoolBulkheadConfig);
            return this;
        }
        public Builder addThreadPoolBulkheadConfig(String configName, ThreadPoolBulkheadConfig configuration) {
            if (configName.equals(DEFAULT_CONFIG)) {
                throw new IllegalArgumentException(
                        "You cannot add another configuration with name 'default' as it is preserved for default configuration");
            }
            threadPoolBulkheadConfigsMap.put(configName, configuration);
            return this;
        }

        public Builder addRegistryEventConsumer(RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer) {
            this.registryEventConsumers.add(registryEventConsumer);
            return this;
        }

        public Builder withTags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public ThreadPoolBulkheadRegistry build() {
            return new InMemoryThreadPoolBulkheadRegistry(threadPoolBulkheadConfigsMap, registryEventConsumers, tags,
                    registryStore);
        }
    }

    static Builder custom() {
        return new Builder();
    }

}
