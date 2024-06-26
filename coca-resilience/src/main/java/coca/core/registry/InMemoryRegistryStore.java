package coca.core.registry;

import coca.core.RegistryStore;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class InMemoryRegistryStore<E> implements RegistryStore<E> {
    private ConcurrentHashMap<String,E> entryMap = new ConcurrentHashMap<>();

    public InMemoryRegistryStore() {
    }

    @Override
    public E computeIfAbsent(String key, Function<? super String, ? extends E> mappingFunction) {
        return entryMap.computeIfAbsent(key,mappingFunction);
    }

    @Override
    public E putIfAbsent(String key, E value) {
        return entryMap.putIfAbsent(key, value);
    }

    @Override
    public Optional<E> find(String key) {
        return Optional.ofNullable(entryMap.get(key));
    }

    @Override
    public Optional<E> remove(String name) {
        return Optional.ofNullable(entryMap.remove(name));
    }

    @Override
    public Optional<E> replace(String name, E newEntry) {
        return Optional.ofNullable(entryMap.replace(name,newEntry));
    }

    @Override
    public Collection<E> values() {
        return entryMap.values();
    }
}
