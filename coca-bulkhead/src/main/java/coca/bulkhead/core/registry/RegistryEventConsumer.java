package coca.bulkhead.core.registry;

import coca.bulkhead.ThreadPoolBulkhead;

public interface RegistryEventConsumer<E> {

    void onEntryAddedEvent(EntryAddedEvent<E> entryAddedEvent);

    void onEntryRemovedEvent(EntryRemovedEvent<E> entryRemoveEvent);

    void onEntryReplacedEvent(EntryReplacedEvent<E> entryReplacedEvent);
}
