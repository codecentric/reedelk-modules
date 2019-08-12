package com.reedelk.esb.execution.scheduler;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FlowScheduler {

    public static class FlowSchedulerConfig {
        private final int poolMinSize;
        private final int poolMaxSize;
        private final int keepAliveTime;

        public FlowSchedulerConfig(int poolMinSize, int poolMaxSize, int keepAliveTime) {
            this.poolMinSize = poolMinSize;
            this.poolMaxSize = poolMaxSize;
            this.keepAliveTime = keepAliveTime;
        }
    }

    private static volatile FlowScheduler INSTANCE;

    private final Scheduler scheduler;

    private FlowScheduler(FlowSchedulerConfig config) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                config.poolMinSize, config.poolMaxSize, config.keepAliveTime, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new DefaultThreadFactory("Flow-pool"));
        scheduler = Schedulers.fromExecutorService(threadPoolExecutor, "Flow-pool");
    }

    static void initialize(FlowSchedulerConfig config) {
        if (INSTANCE == null) {
            synchronized (FlowScheduler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FlowScheduler(config);
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
