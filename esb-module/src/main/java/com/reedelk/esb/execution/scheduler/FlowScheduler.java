package com.reedelk.esb.execution.scheduler;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class FlowScheduler {

    private static volatile FlowScheduler INSTANCE;

    final Scheduler scheduler;

    private FlowScheduler(int flowPoolMaxSize) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                0, flowPoolMaxSize,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new DefaultThreadFactory("Flow-pool"));
        scheduler = Schedulers.fromExecutorService(threadPoolExecutor, "Flow-pool");
    }

    static void initialize(int flowPoolMaxSize) {
        if (INSTANCE == null) {
            synchronized (FlowScheduler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FlowScheduler(flowPoolMaxSize);
                }
            }
        }
    }

    static Scheduler scheduler() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Error, scheduler not initialized");
        }
        return INSTANCE.scheduler;
    }
}
