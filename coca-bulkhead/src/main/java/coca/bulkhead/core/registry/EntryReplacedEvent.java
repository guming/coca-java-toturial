package coca.bulkhead.core.registry;

public class EntryReplacedEvent<E> extends  AbstractRegistryEvent{
    private E replacedEntry;

    public EntryReplacedEvent(E replacedEntry) {
        this.replacedEntry = replacedEntry;
    }

    public E getReplacedEntry() {
        return replacedEntry;
    }

    @Override
    public Type getEventType() {
        return Type.REPLACED;
    }
}
