package coca.bulkhead.event;

import java.time.ZonedDateTime;

public abstract class AbstractBulkheadEvent implements BulkheadEvent {
    private final ZonedDateTime creationTime;
    private final String bulkheadName;

    public AbstractBulkheadEvent(String bulkheadName) {
        this.creationTime = ZonedDateTime.now();;
        this.bulkheadName = bulkheadName;
    }

    @Override
    public String getBulkheadName() {
        return bulkheadName;
    }

    @Override
    public ZonedDateTime getCreationTime() {
        return creationTime;
    }
}
