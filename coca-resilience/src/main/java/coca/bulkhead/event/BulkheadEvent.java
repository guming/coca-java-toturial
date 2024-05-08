package coca.bulkhead.event;

import java.time.ZonedDateTime;

public interface BulkheadEvent {
    String getBulkheadName();

    Type getEventType();

    ZonedDateTime getCreationTime();
    enum Type {
        CALL_PERMITTED,
        CALL_REJECTED,
        CALL_FINISHED
    }
}
