package coca.ratelimiter.internal;

import coca.ratelimiter.RateLimiter;
import coca.ratelimiter.RateLimiterConfig;
import coca.ratelimiter.event.RateLimiterOnFailureEvent;
import coca.ratelimiter.event.RateLimiterOnSuccessEvent;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.min;
import static java.lang.System.nanoTime;
import static java.util.concurrent.locks.LockSupport.parkNanos;

public class AtomicRateLimiter implements RateLimiter {

    private final String name;
    private final Map<String, String> tags;
    private final long nanoTimeStart;
    private final RateLimiterEventProcessor processor;
    private final AtomicReference<State> state;

    public AtomicRateLimiter(Map<String, String> tags, String name, RateLimiterConfig config) {
        this.tags = tags;
        this.name = name;
        this.nanoTimeStart = nanoTime();
        processor = new RateLimiterEventProcessor();
        state = new AtomicReference<>(new State(config,0,config.getLimitForPeriod(),0));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public EventPublisher getEventPublisher() {
        return processor;
    }

    @Override
    public boolean acquirePermission(int permits) {
        long timeoutNanos = state.get().config.getTimeoutDuration().toNanos();
        State updated = updateStateWithBackoff(permits, timeoutNanos);
        boolean result = waitForPermissionIfNecessary(timeoutNanos, updated.nanosToWait);
        publishRateLimiterAcquisitionEvent(result, permits);
        return result;
    }

    @Override
    public long reservePermission(int permits) {
        long timeoutNanos = state.get().config.getTimeoutDuration().toNanos();
        State updated = updateStateWithBackoff(permits, timeoutNanos);
        if(updated.nanosToWait<=0){
            publishRateLimiterAcquisitionEvent(true, permits);
            return 0;
        }

        if(timeoutNanos>=updated.nanosToWait){
            publishRateLimiterAcquisitionEvent(true,permits);
            return updated.nanosToWait;
        }
        publishRateLimiterAcquisitionEvent(false,permits);
        return -1;
    }

    @Override
    public void drainPermissions() {

    }

    @Override
    public void changeTimeoutDuration(Duration timeoutDuration) {

    }

    @Override
    public void changeLimitForPeriod(int limitForPeriod) {

    }

    private static class State {
        private final RateLimiterConfig config;
        private final int activePermissions;
        private final long nanosToWait;
        private final long activeCycle;

        public State(RateLimiterConfig config, int activePermissions, long nanosToWait, long activeCycle) {
            this.config = config;
            this.activePermissions = activePermissions;
            this.nanosToWait = nanosToWait;
            this.activeCycle = activeCycle;
        }
    }

    private void publishRateLimiterAcquisitionEvent(boolean permissionAcquired, int permits) {
        if(!processor.hasConsumer()){
            return;
        }
        if(permissionAcquired) {
            processor.consumeEvent(new RateLimiterOnSuccessEvent(permits, name));
            return;
        }
        processor.consumeEvent(new RateLimiterOnFailureEvent(permits,name));
    }

    private State updateStateWithBackoff(int permits, long timeoutNanos) {
        AtomicRateLimiter.State prev;
        AtomicRateLimiter.State next;
        do {
            prev = state.get();
            next = caculateNextState(permits, timeoutNanos, prev);
        } while (!compareAndSet(prev,next));
        return next;
    }

    private boolean compareAndSet(final State current, final State next) {
        if (state.compareAndSet(current, next)) {
            return true;
        }
        parkNanos(1);
        return false;
    }

    private boolean waitForPermissionIfNecessary(long timeoutNanos, long nanosToWait) {

        return false;
    }

    private State caculateNextState(int permits, long timeoutNanos, State prev) {
        long cycleRefreshInterval = prev.config.getLimitRefreshInterval().toNanos();
        int permissionsPerCycle = prev.config.getLimitForPeriod();
        long current = System.nanoTime();
        long currentNanos = current - nanoTimeStart;
        long currentCycle = currentNanos / cycleRefreshInterval;

        long nextCycle = prev.activeCycle;
        int nextPermissions = prev.activePermissions;
        if (prev.activeCycle != currentCycle) {
            long elapsedCycles = currentCycle - prev.activeCycle;
            long accumlatedPermissions = elapsedCycles * permissionsPerCycle;
            nextCycle = currentCycle;
            nextPermissions = (int) min(nextPermissions + accumlatedPermissions, permissionsPerCycle);
        }
        long nanosToWait = nanosToWaitPermission(permits, cycleRefreshInterval, permissionsPerCycle, nextPermissions, currentNanos,currentCycle);
        State nextState = reservePermissions(prev.config,permits,timeoutNanos,nextCycle,nextPermissions,nanosToWait);
        return nextState;
    }

    private State reservePermissions(RateLimiterConfig config, int permits, long timeoutNanos,
                                     long cycle, int permissions, long nanosToWait){
        boolean canAcquireInTime = timeoutNanos >= nanosToWait;
        int permissionWithReservation = permissions;
        if(canAcquireInTime){
            permissionWithReservation -= permits;
        }
        return new State(config, permissionWithReservation, nanosToWait, cycle);
    }

    private long nanosToWaitPermission(int permits, long cycleRefreshInterval, int permissionsPerCycle, int availablePermissions,
                                       long currentNanos, long currentCycle){
        if(availablePermissions>permits){
            return 0;
        }
        long nextCycleTimeInNanos = (currentCycle + 1) * cycleRefreshInterval;
        long nanosToNextCycle = nextCycleTimeInNanos - currentNanos;
        int permissionsAtTheStartOfNextCycle = availablePermissions + permissionsPerCycle;
        int cyclesToWait = divCeil(-(permissionsAtTheStartOfNextCycle - permits), permissionsPerCycle);
        return (cyclesToWait * cycleRefreshInterval) + nanosToNextCycle;
    }
    private static int divCeil(int x, int y) {
        return (x + y - 1) / y;
    }
}
