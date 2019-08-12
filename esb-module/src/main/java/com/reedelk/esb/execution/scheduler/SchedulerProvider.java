package com.reedelk.esb.execution.scheduler;

import reactor.core.scheduler.Scheduler;

public class SchedulerProvider {

    public static void initialize(int flowPoolMaxSize) {
        FlowScheduler.initialize(flowPoolMaxSize);
    }

    public static Scheduler flow() {
        return FlowScheduler.scheduler();
    }

    public static Scheduler fork(int threads) {
        return ForkSchedulerProvider.get(threads);
    }
}
