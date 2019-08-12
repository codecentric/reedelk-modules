package com.reedelk.esb.configuration;

import com.reedelk.esb.services.configuration.ESBConfigurationService;

public class RuntimeConfigurationProvider {

    private static final String RUNTIME_CONFIG_FILE_PID = "com.reedelk.runtime";

    private static volatile RuntimeConfigurationProvider INSTANCE;

    private final ESBConfigurationService configService;
    private FlowSchedulerConfig flowSchedulerConfig;

    private RuntimeConfigurationProvider(ESBConfigurationService configService) {
        this.configService = configService;
        loadProperties();
    }

    public static RuntimeConfigurationProvider get() {
        return INSTANCE;
    }

    private void loadProperties() {
        int flowPoolMinSize = configService.getIntConfigProperty(RUNTIME_CONFIG_FILE_PID,
                "executor.scheduler.flow.min.pool.size", 1);

        int flowPoolMaxSize = configService.getIntConfigProperty(RUNTIME_CONFIG_FILE_PID,
                "executor.scheduler.flow.max.pool.size", 30);

        long flowPoolKeepAliveTime = configService.getLongConfigProperty(RUNTIME_CONFIG_FILE_PID,
                "executor.scheduler.flow.keep.alive.time", 60);

        long asyncProcessorTimeout = configService.getLongConfigProperty(RUNTIME_CONFIG_FILE_PID,
                "executor.scheduler.flow.async.processor.timeout", 120000);

        this.flowSchedulerConfig = new FlowSchedulerConfig(
                flowPoolMinSize,
                flowPoolMaxSize,
                flowPoolKeepAliveTime,
                asyncProcessorTimeout);
    }

    public FlowSchedulerConfig getFlowSchedulerConfig() {
        return flowSchedulerConfig;
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

    public static class FlowSchedulerConfig {
        private final long asyncProcessorTimeout;
        private final long keepAliveTime;
        private final int poolMinSize;
        private final int poolMaxSize;

        private FlowSchedulerConfig(int poolMinSize, int poolMaxSize, long keepAliveTime, long asyncProcessorTimeout) {
            this.asyncProcessorTimeout = asyncProcessorTimeout;
            this.keepAliveTime = keepAliveTime;
            this.poolMinSize = poolMinSize;
            this.poolMaxSize = poolMaxSize;
        }

        public long getAsyncProcessorTimeout() {
            return asyncProcessorTimeout;
        }

        public long getKeepAliveTime() {
            return keepAliveTime;
        }

        public int getPoolMinSize() {
            return poolMinSize;
        }

        public int getPoolMaxSize() {
            return poolMaxSize;
        }
    }
}
