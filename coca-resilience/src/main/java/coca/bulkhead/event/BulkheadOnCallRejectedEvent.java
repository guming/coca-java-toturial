package coca.bulkhead.event;

public class BulkheadOnCallRejectedEvent extends AbstractBulkheadEvent {
    public BulkheadOnCallRejectedEvent(String bulkheadName) {
        super(bulkheadName);
    }

    @Override
    public Type getEventType() {
        return Type.CALL_REJECTED;
    }

}
