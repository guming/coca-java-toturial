package coca.bulkhead.event;

public class BulkheadOnCallFinishedEvent extends AbstractBulkheadEvent {
    public BulkheadOnCallFinishedEvent(String bulkheadName) {
        super(bulkheadName);
    }

    @Override
    public Type getEventType() {
        return Type.CALL_FINISHED;
    }
}
