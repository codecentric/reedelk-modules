package com.reedelk.esb.configuration;

import com.reedelk.esb.services.configuration.ESBConfigurationService;

import static com.reedelk.esb.commons.Preconditions.checkState;

public class RuntimeConfigurationProvider {

    private static final String RUNTIME_CONFIG_FILE_PID = "com.reedelk.runtime";

    private static final RuntimeConfigurationProvider PROVIDER = new RuntimeConfigurationProvider();

    private ESBConfigurationService configService;
    private FlowExecutorConfig executorConfig;

    private RuntimeConfigurationProvider() {
    }

    private void init(ESBConfigurationService configService) {
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

    static void initialize(ESBConfigurationService configService) {
        PROVIDER.init(configService);
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
            int queueSize = configService.getIntConfigProperty(RUNTIME_CONFIG_FILE_PID,
                    "executor.scheduler.flow.bounded.queue.size", 200);
            schedulerConfig = new BoundedSchedulerConfig(poolMinSize, poolMaxSize, keepAliveTimeSeconds, queueSize);
        }

        long asyncProcessorTimeoutMillis = configService.getLongConfigProperty(RUNTIME_CONFIG_FILE_PID,
                "executor.scheduler.flow.async.processor.timeout", 120000);


        executorConfig = new FlowExecutorConfig(asyncProcessorTimeoutMillis, schedulerConfig);
    }
}
