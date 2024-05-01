package coca.core.registry;

public interface RegistryEventConsumer<E> {

    void onEntryAddedEvent(EntryAddedEvent<E> entryAddedEvent);

    void onEntryRemovedEvent(EntryRemovedEvent<E> entryRemoveEvent);

    void onEntryReplacedEvent(EntryReplacedEvent<E> entryReplacedEvent);
}
