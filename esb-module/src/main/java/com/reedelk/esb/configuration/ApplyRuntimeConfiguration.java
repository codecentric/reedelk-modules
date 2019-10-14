package com.reedelk.esb.configuration;

import com.reedelk.esb.execution.scheduler.SchedulerProvider;
import com.reedelk.esb.services.configuration.DefaultConfigurationService;

/**
 * Applies the properties from the runtime configuration file. For example
 * it initializes the Flow Executor schedulers with the provided config values.
 */
public class ApplyRuntimeConfiguration {

    private ApplyRuntimeConfiguration() {
    }

    public static void from(DefaultConfigurationService configurationService) {
        RuntimeConfigurationProvider.initialize(configurationService);
        SchedulerProvider.initialize();
    }
}
