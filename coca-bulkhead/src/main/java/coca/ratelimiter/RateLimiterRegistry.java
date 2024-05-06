package coca.ratelimiter;

import coca.core.Registry;
import coca.core.RegistryStore;
import coca.core.registry.RegistryEventConsumer;

import java.util.*;
import java.util.function.Supplier;

public interface RateLimiterRegistry extends Registry<RateLimiter,RateLimiterConfig> {
    Set<RateLimiter> getAllRateLimiters();
    RateLimiter rateLimiter(String name);
    RateLimiter rateLimiter(String name, Map<String, String> tags);
    RateLimiter rateLimiter(String name, RateLimiterConfig rateLimiterConfig);
    RateLimiter rateLimiter(String name, RateLimiterConfig rateLimiterConfig, Map<String, String> tags);
    RateLimiter rateLimiter(String name, Supplier<RateLimiterConfig> rateLimiterConfigSupplier);
    RateLimiter rateLimiter(String name, Supplier<RateLimiterConfig> rateLimiterConfigSupplier, Map<String, String> tags);
    RateLimiter rateLimiter(String name, String configName);
    RateLimiter rateLimiter(String name, String configName, Map<String, String> tags);
    static Builder custom(){
        return new Builder();
    }
    class Builder {
        private final String DEFAULT_CONFIG = "default";
        private RegistryStore<RateLimiter> registryStore;
        private Map<String, RateLimiterConfig> registryConfigs;
        private List<RegistryEventConsumer<RateLimiter>> registryEventConsumers;
        private Map<String,String> tags;

        public Builder() {
            this.registryConfigs = new HashMap<>();
            this.registryEventConsumers = new ArrayList<>();
        }

        public Builder withRegistryStore(RegistryStore<RateLimiter> registryStore) {
            this.registryStore = registryStore;
            return this;
        }
        public Builder withRateLimiterConfig(RateLimiterConfig rateLimiterConfig) {
            registryConfigs.put(DEFAULT_CONFIG, rateLimiterConfig);
            return this;
        }
        public Builder withTags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder addRateLimiterConfig(String configName, RateLimiterConfig configuration) {
            if (configName.equals(DEFAULT_CONFIG)) {
                throw new IllegalArgumentException(
                        "You cannot add another configuration with name 'default' as it is preserved for default configuration");
            }
            registryConfigs.put(configName, configuration);
            return this;
        }

        public Builder addRegistryEventConsumer(RegistryEventConsumer<RateLimiter> registryEventConsumer) {
            this.registryEventConsumers.add(registryEventConsumer);
            return this;
        }

        public RateLimiterRegistry build() {
            return null;
        }

    }

}
