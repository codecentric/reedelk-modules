package com.reedelk.esb.execution.scheduler;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ForkSchedulerProvider {

    public static Scheduler get(int threads) {
        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(0, threads, 60L, TimeUnit.SECONDS,
                        new SynchronousQueue<>(),
                        new DefaultThreadFactory("Fork-pool"));
        return Schedulers.fromExecutorService(threadPoolExecutor, "Fork-pool");
    }
}
