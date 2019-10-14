package com.reedelk.esb.configuration;

import com.reedelk.esb.services.configuration.DefaultConfigurationService;

import static com.reedelk.esb.commons.Preconditions.checkState;

public class RuntimeConfigurationProvider {

    private static final String RUNTIME_CONFIG_FILE_PID = "com.reedelk.runtime";

    private static final RuntimeConfigurationProvider PROVIDER = new RuntimeConfigurationProvider();

    private DefaultConfigurationService configService;
    private FlowExecutorConfig executorConfig;

    private RuntimeConfigurationProvider() {
    }

    private void init(DefaultConfigurationService configService) {
        synchronized (PROVIDER) {
            checkState(this.configService == null, "Config service already initialized");
            this.configService = configService;
            loadProperties();
        }
    }

    public static RuntimeConfigurationProvider get() {
        return PROVIDER;
    }

    public FlowExecutorConfig getFlowExecutorConfig() {
        return executorConfig;
    }

    static void initialize(DefaultConfigurationService configService) {
        PROVIDER.init(configService);
    }

    private void loadProperties() {
        boolean isUnbounded = configService.getBoolean(RUNTIME_CONFIG_FILE_PID,
                "executor.scheduler.flow.unbounded", true);

        SchedulerConfig schedulerConfig;
        if (isUnbounded) {
            int keepAliveTimeSeconds = configService.getInt(RUNTIME_CONFIG_FILE_PID,
                    "executor.scheduler.flow.unbounded.keep.alive.time", 60);
            schedulerConfig = new UnboundedSchedulerConfig(keepAliveTimeSeconds);

        } else {
            int poolMinSize = configService.getInt(RUNTIME_CONFIG_FILE_PID,
                    "executor.scheduler.flow.bounded.min.pool.size", 1);
            int poolMaxSize = configService.getInt(RUNTIME_CONFIG_FILE_PID,
                    "executor.scheduler.flow.bounded.max.pool.size", 30);
            int keepAliveTimeSeconds = configService.getInt(RUNTIME_CONFIG_FILE_PID,
                    "executor.scheduler.flow.bounded.keep.alive.time", 60);
            int queueSize = configService.getInt(RUNTIME_CONFIG_FILE_PID,
                    "executor.scheduler.flow.bounded.queue.size", 200);
            schedulerConfig = new BoundedSchedulerConfig(poolMinSize, poolMaxSize, keepAliveTimeSeconds, queueSize);
        }

        long asyncProcessorTimeoutMillis = configService.getLong(RUNTIME_CONFIG_FILE_PID,
                "executor.scheduler.flow.async.processor.timeout", 120000);


        executorConfig = new FlowExecutorConfig(asyncProcessorTimeoutMillis, schedulerConfig);
    }
}
