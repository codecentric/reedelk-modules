package com.reedelk.esb.execution.scheduler;

import com.reedelk.esb.configuration.FlowExecutorConfig;
import com.reedelk.esb.configuration.RuntimeConfigurationProvider;
import com.reedelk.esb.configuration.SchedulerConfig;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FlowScheduler {

    private static final String THREAD_POOL_NAME_PREFIX = "Flow-pool";
    private static volatile FlowScheduler INSTANCE;

    private final Scheduler scheduler;

    private FlowScheduler(SchedulerConfig config) {
        if (config.isBounded()) {
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                    config.poolMinSize(), config.poolMaxSize(), config.keepAliveTime(), TimeUnit.SECONDS,
                    new SynchronousQueue<>(), new DefaultThreadFactory(THREAD_POOL_NAME_PREFIX));
            scheduler = Schedulers.fromExecutorService(threadPoolExecutor, THREAD_POOL_NAME_PREFIX);

        } else {
            scheduler = Schedulers.newElastic(THREAD_POOL_NAME_PREFIX, config.keepAliveTime());
        }
    }

    static void initialize() {
        if (INSTANCE == null) {
            synchronized (FlowScheduler.class) {
                if (INSTANCE == null) {
                    RuntimeConfigurationProvider configProvider = RuntimeConfigurationProvider.get();
                    FlowExecutorConfig executorConfig = configProvider.getFlowExecutorConfig();
                    SchedulerConfig schedulerConfig = executorConfig.schedulerConfig();
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
