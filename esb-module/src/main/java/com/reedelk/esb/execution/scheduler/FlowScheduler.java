package com.reedelk.esb.execution.scheduler;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class FlowScheduler {

    // TODO: This one should be a system property
    private static final int MAX_POOL_SIZE = 50;

    static final Scheduler INSTANCE;
    static {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                0, MAX_POOL_SIZE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new DefaultThreadFactory("Flow-pool"));
        INSTANCE = Schedulers.fromExecutorService(threadPoolExecutor, "Flow-pool");
    }
}
