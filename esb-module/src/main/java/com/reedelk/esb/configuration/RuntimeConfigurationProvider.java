package com.reedelk.esb.configuration;

import com.reedelk.esb.services.configuration.ESBConfigurationService;

public class RuntimeConfigurationProvider {

    private static final String RUNTIME_CONFIG_FILE_PID = "com.reedelk.runtime";

    private static volatile RuntimeConfigurationProvider INSTANCE;

    private final ESBConfigurationService configService;
    private FlowExecutorConfig executorConfig;

    private RuntimeConfigurationProvider(ESBConfigurationService configService) {
        this.configService = configService;
        loadProperties();
    }

    public static RuntimeConfigurationProvider get() {
        return INSTANCE;
    }

    public FlowExecutorConfig getFlowExecutorConfig() {
        return executorConfig;
    }

    static void initialize(ESBConfigurationService configService) {
        if (INSTANCE == null) {
            synchronized (RuntimeConfigurationProvider.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RuntimeConfigurationProvider(configService);
                }
            }
        }
    }

    private void loadProperties() {
        boolean isUnbounded = configService.getBooleanConfigProperty(RUNTIME_CONFIG_FILE_PID,
                "executor.scheduler.flow.unbounded", true);

        SchedulerConfig schedulerConfig;
        if (isUnbounded) {
            int keepAliveTimeSeconds = configService.getIntConfigProperty(RUNTIME_CONFIG_FILE_PID,
                    "executor.scheduler.flow.unbounded.keep.alive.time", 60);
            schedulerConfig = new UnboundedSchedulerConfig(keepAliveTimeSeconds);

        } else {
            int poolMinSize = configService.getIntConfigProperty(RUNTIME_CONFIG_FILE_PID,
                    "executor.scheduler.flow.bounded.min.pool.size", 1);
            int poolMaxSize = configService.getIntConfigProperty(RUNTIME_CONFIG_FILE_PID,
                    "executor.scheduler.flow.bounded.max.pool.size", 30);
            int keepAliveTimeSeconds = configService.getIntConfigProperty(RUNTIME_CONFIG_FILE_PID,
                    "executor.scheduler.flow.bounded.keep.alive.time", 60);
            schedulerConfig = new BoundedSchedulerConfig(poolMinSize, poolMaxSize, keepAliveTimeSeconds);
        }

        long asyncProcessorTimeoutMillis = configService.getLongConfigProperty(RUNTIME_CONFIG_FILE_PID,
                "executor.scheduler.flow.async.processor.timeout", 120000);


        executorConfig = new FlowExecutorConfig(asyncProcessorTimeoutMillis, schedulerConfig);
    }
}
