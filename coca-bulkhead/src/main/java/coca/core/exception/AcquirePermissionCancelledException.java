package coca.core.exception;

public class AcquirePermissionCancelledException extends IllegalStateException {

    private static final String DEFAULT_MESSAGE = "Thread was interrupted while waiting for a permission";

    public AcquirePermissionCancelledException() {
        super(DEFAULT_MESSAGE);
    }

    public AcquirePermissionCancelledException(String message) {
        super(message);
    }
}
