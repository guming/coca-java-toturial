package coca.ratelimiter.internal;

import coca.core.EventConsumer;
import coca.core.EventProcessor;
import coca.ratelimiter.RateLimiter;
import coca.ratelimiter.event.RateLimiterEvent;
import coca.ratelimiter.event.RateLimiterOnFailureEvent;
import coca.ratelimiter.event.RateLimiterOnSuccessEvent;

public class RateLimiterEventProcessor extends EventProcessor<RateLimiterEvent> implements EventConsumer<RateLimiterEvent>, RateLimiter.EventPublisher {
    @Override
    public void consumeEvent(RateLimiterEvent event) {
        super.processEvent(event);
    }

    @Override
    public RateLimiter.EventPublisher onSuccess(EventConsumer<RateLimiterOnSuccessEvent> consumer) {
        registerConsumer(RateLimiterOnSuccessEvent.class.getName(),consumer);
        return this;
    }

    @Override
    public RateLimiter.EventPublisher onFailure(EventConsumer<RateLimiterOnFailureEvent> consumer) {
        registerConsumer(RateLimiterOnFailureEvent.class.getName(),consumer);
        return this;
    }
}
