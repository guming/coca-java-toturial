package coca.core.registry;

import coca.bulkhead.core.*;
import coca.core.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public class AbstractRegistry<E,C> implements Registry<E,C> {

    protected static final String DEFAULT_CONFIG = "default";
    protected static final String CONFIG_MUST_NOT_BE_NULL = "Config must not be null";
    protected static final String CONSUMER_MUST_NOT_BE_NULL = "EventConsumers must not be null";
    protected static final String SUPPLIER_MUST_NOT_BE_NULL = "Supplier must not be null";
    protected static final String TAGS_MUST_NOT_BE_NULL = "Tags must not be null";
    private static final String NAME_MUST_NOT_BE_NULL = "Name must not be null";
    private static final String REGISTRY_STORE_MUST_NOT_BE_NULL = "Registry Store must not be null";

    protected final RegistryStore<E> entryMap;

    protected final ConcurrentMap<String, C> configurations;

    protected final Map<String, String> registryTags;

    private final RegistryEventProcessor eventProcessor;

    public AbstractRegistry(C defaultConfig) {
        this(defaultConfig, Collections.emptyMap());
    }

    public AbstractRegistry(C defaultConfig, Map<String, String> registryTags) {
        this(defaultConfig, new ArrayList<>(), registryTags);
    }

    public AbstractRegistry(C defaultConfig, RegistryEventConsumer<E> registryEventConsumer) {
        this(defaultConfig, registryEventConsumer, Collections.emptyMap());
    }

    public AbstractRegistry(C defaultConfig, RegistryEventConsumer<E> registryEventConsumer,
                            Map<String, String> tags) {
        this(defaultConfig, Collections.singletonList(
                Objects.requireNonNull(registryEventConsumer, CONSUMER_MUST_NOT_BE_NULL)), tags);
    }

    public AbstractRegistry(C defaultConfig,
                            List<RegistryEventConsumer<E>> registryEventConsumers) {
        this(defaultConfig, registryEventConsumers, Collections.emptyMap());
    }

    public AbstractRegistry(C defaultConfig, List<RegistryEventConsumer<E>> registryEventConsumers,
                            Map<String, String> tags) {
        this.configurations = new ConcurrentHashMap<>();
        this.entryMap = new InMemoryRegistryStore<E>();
        this.eventProcessor = new RegistryEventProcessor(
                registryEventConsumers);
        this.registryTags = Objects.requireNonNull(tags, TAGS_MUST_NOT_BE_NULL);
        this.configurations
                .put(DEFAULT_CONFIG, Objects.requireNonNull(defaultConfig, CONFIG_MUST_NOT_BE_NULL));
    }

    public AbstractRegistry(C defaultConfig, List<RegistryEventConsumer<E>> registryEventConsumers,
                            Map<String, String> tags, RegistryStore<E> registryStore) {
        this.configurations = new ConcurrentHashMap<>();
        this.entryMap = Objects.requireNonNull(registryStore, REGISTRY_STORE_MUST_NOT_BE_NULL);
        this.eventProcessor = new RegistryEventProcessor(
                registryEventConsumers);
        this.registryTags = Objects.requireNonNull(tags, TAGS_MUST_NOT_BE_NULL);
        this.configurations
                .put(DEFAULT_CONFIG, Objects.requireNonNull(defaultConfig, CONFIG_MUST_NOT_BE_NULL));
    }

    protected E computeIfAbsent(String name, Supplier<E> supplier) {
        return entryMap.computeIfAbsent(Objects.requireNonNull(name, NAME_MUST_NOT_BE_NULL), k -> {
            E entry = supplier.get();
            eventProcessor.processEvent(new EntryAddedEvent<>(entry));
            return entry;
        });
    }

    protected Map<String, String> getAllTags(Map<String, String> tags) {
        final HashMap<String, String> allTags = new HashMap<>(Objects.requireNonNull(registryTags, TAGS_MUST_NOT_BE_NULL));
        allTags.putAll(tags);
        return allTags;
    }

    @Override
    public void addConfiguration(String configName, C configuration) {
        this.configurations.put(configName, configuration);
    }

    @Override
    public Optional<E> find(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<E> remove(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<E> replace(String name, E newEntry) {
        return Optional.empty();
    }

    @Override
    public Optional<C> getConfiguration(String configName) {
        return Optional.ofNullable(this.configurations.get(configName));
    }

    @Override
    public C getDefaultConfig() {
        return this.configurations.get(DEFAULT_CONFIG);
    }

    @Override
    public Map<String, String> getTags() {
        return this.registryTags;
    }

    @Override
    public EventPublisher<E> getEventPublisher() {
        return eventProcessor;
    }

    @Override
    public C removeConfiguration(String configName) {
        return this.configurations.remove(configName);
    }

    private class RegistryEventProcessor extends EventProcessor<RegistryEvent> implements EventPublisher<E>, EventConsumer<RegistryEvent> {

        public RegistryEventProcessor(List<RegistryEventConsumer<E>> registryEventConsumers) {
            registryEventConsumers.forEach(consumer -> {
                onEntryAdded(consumer::onEntryAddedEvent);
                onEntryRemoved(consumer::onEntryRemovedEvent);
                onEntryReplaced(consumer::onEntryReplacedEvent);
            });
        }

        @Override
        public void consumeEvent(RegistryEvent event) {
            super.processEvent(event);
        }

        @Override
        public EventPublisher<E> onEntryAdded(EventConsumer<EntryAddedEvent<E>> eventConsumer) {
            registerConsumer(EntryAddedEvent.class.getSimpleName(),eventConsumer);
            return this;
        }

        @Override
        public EventPublisher<E> onEntryRemoved(EventConsumer<EntryRemovedEvent<E>> eventConsumer) {
            registerConsumer(EntryRemovedEvent.class.getSimpleName(),eventConsumer);
            return this;
        }

        @Override
        public EventPublisher<E> onEntryReplaced(EventConsumer<EntryReplacedEvent<E>> eventConsumer) {
            registerConsumer(EntryReplacedEvent.class.getSimpleName(),eventConsumer);
            return this;
        }
    }
}
