package com.reedelk.esb.execution.scheduler;

import com.reedelk.esb.configuration.RuntimeConfigurationProvider;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.reedelk.esb.configuration.RuntimeConfigurationProvider.FlowSchedulerConfig;

public class FlowScheduler {

    private static volatile FlowScheduler INSTANCE;

    private final Scheduler scheduler;

    private FlowScheduler(FlowSchedulerConfig config) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                config.getPoolMinSize(), config.getPoolMaxSize(), config.getKeepAliveTime(), TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(), new DefaultThreadFactory("Flow-pool"));
        scheduler = Schedulers.fromExecutorService(threadPoolExecutor, "Flow-pool");
    }

    static void initialize() {
        if (INSTANCE == null) {
            synchronized (FlowScheduler.class) {
                if (INSTANCE == null) {
                    RuntimeConfigurationProvider configProvider = RuntimeConfigurationProvider.get();
                    FlowSchedulerConfig schedulerConfig = configProvider.getFlowSchedulerConfig();
                    INSTANCE = new FlowScheduler(schedulerConfig);
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
