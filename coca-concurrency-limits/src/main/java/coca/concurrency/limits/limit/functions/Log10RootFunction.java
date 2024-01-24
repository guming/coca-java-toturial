package coca.concurrency.limits.limit.functions;

import java.util.function.Function;
import java.util.stream.IntStream;

public final class Log10RootFunction implements Function<Integer,Integer>{
    private static final Log10RootFunction INSTANCE = new Log10RootFunction();
    static final int[] lookup = new int[1000];
    static {
        IntStream.range(0,1000).forEach(i->lookup[i] = Math.max(1,(int)Math.log10(i)));
    }

    public static Function<Integer,Integer> create(int baseline){
        return INSTANCE.andThen(t -> t + baseline);
    }

    @Override
    public Integer apply(Integer i) {
        return i<1000? lookup[i] : (int)Math.log10(i);
    }
}
