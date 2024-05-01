package coca.core.registry;

public class EntryRemovedEvent<E> extends AbstractRegistryEvent{
    private E removedEntry;

    public EntryRemovedEvent(E removedEntry) {
        this.removedEntry = removedEntry;
    }

    public E getRemovedEntry() {
        return removedEntry;
    }

    @Override
    public Type getEventType() {
        return Type.REMOVED;
    }
}
