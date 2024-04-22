package org.coca.alg.structure;

import java.util.concurrent.TimeUnit;
import java.util.Set;

public interface Timer {

    Timeout newTimeout(TimerTask timerTask, long delay, TimeUnit unit);

    Set<Timeout> stop();

}
