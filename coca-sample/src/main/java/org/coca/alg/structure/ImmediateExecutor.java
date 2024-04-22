package org.coca.alg.structure;

import java.util.concurrent.Executor;

public class ImmediateExecutor implements Executor {
    public static final ImmediateExecutor INSTANCE = new ImmediateExecutor();

    private ImmediateExecutor() {
        // use static instance
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
