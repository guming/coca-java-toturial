package coca.bulkhead.core;

import coca.bulkhead.core.registry.EntryAddedEvent;
import coca.bulkhead.core.registry.EntryRemovedEvent;
import coca.bulkhead.core.registry.EntryReplacedEvent;
import coca.bulkhead.core.registry.RegistryEvent;

import java.util.Map;
import java.util.Optional;

public interface Registry<E,C> {

    void addConfiguration(String configName, C configuration);

    Optional<E> find(String name);

    Optional<E> remove(String name);

    Optional<E> replace(String name, E newEntry);

    Optional<C> getConfiguration(String configName);

    C getDefaultConfig();

    Map<String, String> getTags();

    EventPublisher<E> getEventPublisher();

    C removeConfiguration(String configName);

    interface EventPublisher<E> extends coca.bulkhead.core.EventPublisher<RegistryEvent>{

        EventPublisher<E> onEntryAdded(EventConsumer<EntryAddedEvent<E>> eventConsumer);

        EventPublisher<E> onEntryRemoved(EventConsumer<EntryRemovedEvent<E>> eventConsumer);

        EventPublisher<E> onEntryReplaced(EventConsumer<EntryReplacedEvent<E>> eventConsumer);

    }

}
