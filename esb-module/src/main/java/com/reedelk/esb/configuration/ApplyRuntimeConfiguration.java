package com.reedelk.esb.configuration;

import com.reedelk.esb.execution.scheduler.SchedulerProvider;
import com.reedelk.esb.services.configuration.ESBConfigurationService;

/**
 * Applies the properties from the runtime configuration file. For example
 * it initializes the Flow Executor Schedulers with the provided config values.
 */
public class ApplyRuntimeConfiguration {

    private static final String RUNTIME_CONFIG_FILE_PID = "com.reedelk.runtime";

    public static void from(ESBConfigurationService configurationService) {
        int flowExecutorMaxPoolSize = configurationService.getIntConfigProperty(RUNTIME_CONFIG_FILE_PID,
                "executor.scheduler.flow.max.pool.size", 50);
        SchedulerProvider.initialize(flowExecutorMaxPoolSize);
    }
}
