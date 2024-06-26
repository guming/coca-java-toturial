package coca.ratelimiter.internal;

import coca.ratelimiter.RateLimiter;
import coca.ratelimiter.RateLimiterConfig;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class RateLimiterEventPublisherTest {
    private static final int LIMIT = 1;
    private static final Duration TIMEOUT = Duration.ZERO;
    private static final Duration REFRESH_PERIOD = Duration.ofSeconds(5);

    private RateLimiter rateLimiter;
    private Logger logger;

    @Before
    public void setUp() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .timeoutDuration(TIMEOUT)
                .limitRefreshInterval(REFRESH_PERIOD)
                .limitForPeriod(LIMIT)
                .build();
        rateLimiter = RateLimiter.of("test", config);
        logger = mock(Logger.class);
    }

    @Test
    public void shouldReturnTheSameConsumer() {
        RateLimiter.EventPublisher eventPublisher = rateLimiter.getEventPublisher();
        RateLimiter.EventPublisher eventPublisher2 = rateLimiter.getEventPublisher();

        assertThat(eventPublisher).isEqualTo(eventPublisher2);
    }


    @Test
    public void shouldConsumeOnSuccessEvent() throws Throwable {
        rateLimiter.getEventPublisher().onSuccess(
                event -> logger.info(event.getEventType().toString()));

        String result = rateLimiter.executeSupplier(() -> "Hello world");

        assertThat(result).isEqualTo("Hello world");
        then(logger).should(times(1)).info("SUCCESSFUL_ACQUIRE");
    }

    @Test
    public void shouldConsumeOnFailureEvent() throws Throwable {
        rateLimiter.getEventPublisher().onFailure(
                event -> logger.info(event.getEventType().toString()));
        rateLimiter.executeSupplier(() -> "Hello world");

        Try.ofSupplier(RateLimiter.decorateSupplier(rateLimiter, () -> "Hello world"));

        then(logger).should(times(1)).info("FAILED_ACQUIRE");
    }
}
