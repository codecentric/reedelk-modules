package com.reedelk.esb.configuration;

import com.reedelk.esb.execution.scheduler.SchedulerProvider;
import com.reedelk.esb.services.configuration.ESBConfigurationService;

import static com.reedelk.esb.execution.scheduler.FlowScheduler.FlowSchedulerConfig;

/**
 * Applies the properties from the runtime configuration file. For example
 * it initializes the Flow Executor schedulers with the provided config values.
 */
public class ApplyRuntimeConfiguration {

    private static final String RUNTIME_CONFIG_FILE_PID = "com.reedelk.runtime";

    public static void from(ESBConfigurationService configurationService) {
        int flowPoolMinSize = configurationService.getIntConfigProperty(RUNTIME_CONFIG_FILE_PID,
                "executor.scheduler.flow.min.pool.size", 1);

        int flowPoolMaxSize = configurationService.getIntConfigProperty(RUNTIME_CONFIG_FILE_PID,
                "executor.scheduler.flow.max.pool.size", 30);

        int flowPoolKeepAliveTime = configurationService.getIntConfigProperty(RUNTIME_CONFIG_FILE_PID,
                "executor.scheduler.flow.keep.alive.time", 60);

        FlowSchedulerConfig config
                = new FlowSchedulerConfig(flowPoolMinSize, flowPoolMaxSize, flowPoolKeepAliveTime);

        SchedulerProvider.initialize(config);
    }
}
