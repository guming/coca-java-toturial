package coca.bulkhead.core.registry;

import java.time.ZonedDateTime;

public interface RegistryEvent {

    Type getEventType();

    ZonedDateTime getCreationTime();

    enum Type {
        /**
         * An Event which informs that an entry has been added
         */
        ADDED,
        /**
         * An Event which informs that an entry has been removed
         */
        REMOVED,
        /**
         * An Event which informs that an entry has been replaced
         */
        REPLACED
    }
}
