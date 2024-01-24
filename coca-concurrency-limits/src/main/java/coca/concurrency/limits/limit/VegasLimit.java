package coca.concurrency.limits.limit;

import coca.concurrency.limits.MetricRegistry;
import coca.concurrency.limits.MetricValues;
import coca.concurrency.limits.internal.EmptyMetricRegistry;
import coca.concurrency.limits.limit.functions.Log10RootFunction;

import java.lang.reflect.MalformedParametersException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;

public class VegasLimit extends AbstractLimit{
    private static final Function<Integer, Integer> LOG10 = Log10RootFunction.create(0);

    public static VegasLimit newInstance(){
        return newBuilder().build();
    }
    public static Builder newBuilder(){
        return new Builder();
    }


    public static class Builder {
        private int initLimit =20;
        private int maxConcurrency=1000;
        private MetricRegistry registry = EmptyMetricRegistry.INSTANCE;
        private double smoothing = 1.0;
        private Function<Integer, Integer> alphaFunc = (limit) -> 3 * LOG10.apply(limit.intValue());
        private Function<Integer, Integer> betaFunc = (limit) -> 6 * LOG10.apply(limit.intValue());
        private Function<Integer, Integer> thresholdFunc = (limit) -> LOG10.apply(limit.intValue());
        private Function<Double, Double> increaseFunc = (limit) -> limit + LOG10.apply(limit.intValue());
        private Function<Double, Double> decreaseFunc = (limit) -> limit - LOG10.apply(limit.intValue());
        private int probeMultiplier = 30;
        private Builder(){
        }
        public Builder probeMultiplier(int probeMultiplier) {
            this.probeMultiplier = probeMultiplier;
            return this;
        }
        public Builder alpha(int alpha){
            this.alphaFunc = (ignore) -> alpha;
            return this;
        }
        public Builder threshold(Function<Integer, Integer> threshold) {
            this.thresholdFunc = threshold;
            return this;
        }
        public Builder beta(int beta) {
            this.betaFunc = (ignore) -> beta;
            return this;
        }
        public Builder beta(Function<Integer, Integer> beta) {
            this.betaFunc = beta;
            return this;
        }

        public Builder increase(Function<Double, Double> increase) {
            this.increaseFunc = increase;
            return this;
        }

        public Builder decrease(Function<Double, Double> decrease) {
            this.decreaseFunc = decrease;
            return this;
        }

        public Builder smoothing(double smoothing) {
            this.smoothing = smoothing;
            return this;
        }

        public Builder initLimit(int initLimit) {
            this.initLimit = initLimit;
            return this;
        }
        public Builder maxConcurrency(int maxConcurrency) {
            this.maxConcurrency = maxConcurrency;
            return this;
        }
        public Builder metricRegistry(MetricRegistry registry) {
            this.registry = registry;
            return this;
        }


        public VegasLimit build(){
            return new VegasLimit(this);
        }
    }
    private final int maxLimit;
    private volatile double estimatedLimit;
    private volatile long rtt_noload = 0;
    private final double smoothing;
    private final Function<Integer, Integer> alphaFunc;
    private final Function<Integer, Integer> betaFunc;
    private final Function<Integer, Integer> thresholdFunc;
    private final Function<Double, Double> increaseFunc;
    private final Function<Double, Double> decreaseFunc;
    private final MetricRegistry.SampleListener rttSampleListener;
    private final int probeMultiplier;
    private int probeCount = 0;
    private double probeJitter;
    private VegasLimit(Builder builder) {
        super(builder.initLimit);
        this.maxLimit = builder.maxConcurrency;
        this.alphaFunc = builder.alphaFunc;
        this.betaFunc = builder.betaFunc;
        this.increaseFunc = builder.increaseFunc;
        this.decreaseFunc = builder.decreaseFunc;
        this.thresholdFunc = builder.thresholdFunc;
        this.smoothing = builder.smoothing;
        this.probeMultiplier = builder.probeMultiplier;
        this.estimatedLimit = builder.initLimit;
        this.rttSampleListener = builder.registry.distribution(MetricValues.MIN_RTT_NAME);

    }
    private void resetProbeJitter() {
        probeJitter = ThreadLocalRandom.current().nextDouble(0.5, 1);
    }
    private boolean shouldProbe() {
        return probeJitter * probeMultiplier * estimatedLimit <= probeCount;
    }
    @Override
    protected int _update(long startTime, long rtt, int inflight, boolean didDrop) {
        probeCount++;
        if(shouldProbe()){
            resetProbeJitter();
            probeCount=0;
            rtt_noload = rtt;
            return (int) estimatedLimit;
        }
        rttSampleListener.addSamole(rtt_noload);
        return updateEstimatedLimit(rtt, inflight, didDrop);
    }

    private int updateEstimatedLimit(long rtt, int inflight, boolean didDrop) {
        final int queueSize = (int) Math.ceil(estimatedLimit * (double) rtt_noload / rtt);
        double newLimit;
        if(didDrop){
            newLimit = decreaseFunc.apply(estimatedLimit);
        } else if (inflight *2 < estimatedLimit){
            return (int) estimatedLimit;
        } else {
            int alpha = alphaFunc.apply((int)estimatedLimit);
            int beta = betaFunc.apply((int)estimatedLimit);
            int threshold = this.thresholdFunc.apply((int)estimatedLimit);

            if (queueSize <= threshold) {
                newLimit = estimatedLimit + beta;
            } else if (queueSize < alpha){
                newLimit = increaseFunc.apply(estimatedLimit);
            } else if (queueSize > beta) {
                newLimit = decreaseFunc.apply(estimatedLimit);
            } else {
                return (int)estimatedLimit;
            }
        }
        newLimit = Math.max(1, Math.min(maxLimit, newLimit));
        newLimit = (1 - smoothing) * estimatedLimit + smoothing * newLimit;
        estimatedLimit = newLimit;
        return (int)estimatedLimit;
    }
}
