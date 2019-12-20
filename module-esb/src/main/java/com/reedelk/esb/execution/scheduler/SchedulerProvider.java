package com.reedelk.esb.execution.scheduler;

import reactor.core.scheduler.Scheduler;

public class SchedulerProvider {

    private SchedulerProvider() {
    }

    public static void initialize() {
        FlowScheduler.initialize();
    }

    public static Scheduler flow() {
        return FlowScheduler.scheduler();
    }
}