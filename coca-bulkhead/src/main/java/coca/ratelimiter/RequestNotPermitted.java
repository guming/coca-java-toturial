package coca.ratelimiter;

public class RequestNotPermitted extends RuntimeException {
    private RequestNotPermitted(String message) {
        super(message, null, false, true);
    }

    public static RequestNotPermitted createRequestNotPermitted(RateLimiter rateLimiter) {
        String message = String
                .format("RateLimiter '%s' does not permit further calls", rateLimiter.getName());
        return new RequestNotPermitted(message);
    }
}
