package coca.core.registry;

public class EntryAddedEvent<E> extends AbstractRegistryEvent {
    private E addedEntry;

    public EntryAddedEvent(E addedEntry) {
        this.addedEntry = addedEntry;
    }

    @Override
    public Type getEventType() {
        return Type.ADDED;
    }

    public E getAddedEntry() {
        return addedEntry;
    }
}
