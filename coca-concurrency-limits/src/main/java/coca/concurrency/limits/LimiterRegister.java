package coca.concurrency.limits;

public interface LimiterRegister<ContextT> {
    Limiter<ContextT> get(String key);
    static <ContextT> LimiterRegister<ContextT> single(Limiter<ContextT> limiter) {
        return key -> limiter;
    }
}
