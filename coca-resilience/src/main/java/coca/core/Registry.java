package coca.core;

import coca.core.registry.EntryAddedEvent;
import coca.core.registry.EntryRemovedEvent;
import coca.core.registry.EntryReplacedEvent;
import coca.core.registry.RegistryEvent;

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

    interface EventPublisher<E> extends coca.core.EventPublisher<RegistryEvent> {

        EventPublisher<E> onEntryAdded(EventConsumer<EntryAddedEvent<E>> eventConsumer);

        EventPublisher<E> onEntryRemoved(EventConsumer<EntryRemovedEvent<E>> eventConsumer);

        EventPublisher<E> onEntryReplaced(EventConsumer<EntryReplacedEvent<E>> eventConsumer);

    }

}
