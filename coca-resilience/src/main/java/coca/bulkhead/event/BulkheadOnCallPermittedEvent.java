package coca.bulkhead.event;

import java.time.ZonedDateTime;

public class BulkheadOnCallPermittedEvent extends AbstractBulkheadEvent {
    @Override
    public Type getEventType() {
        return Type.CALL_PERMITTED;
    }

    public BulkheadOnCallPermittedEvent(String bulkheadName) {
        super(bulkheadName);
    }

}
