package coca.concurrency.limits;

import java.util.Optional;

public interface Limiter<ContextT> {
    interface Listener {
        void onSuccess();
        void onIgnore();
        void onDropped();
    }
    Optional<Listener> acquired(ContextT context);
}
