package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.exception.ConfigurationException;
import com.reedelk.scheduler.configuration.CronConfiguration;
import com.reedelk.scheduler.configuration.FixedFrequencyConfiguration;
import com.reedelk.scheduler.configuration.SchedulingStrategy;

public class SchedulingStrategyBuilder {

    private SchedulingStrategy strategy;
    private CronConfiguration cronConfig;
    private FixedFrequencyConfiguration fixedFrequencyConfig;

    private SchedulingStrategyBuilder(SchedulingStrategy schedulingStrategy) {
        this.strategy = schedulingStrategy;
    }

    public SchedulingStrategyBuilder withFixedFrequencyConfig(FixedFrequencyConfiguration fixedFrequencyConfiguration) {
        this.fixedFrequencyConfig = fixedFrequencyConfiguration;
        return this;
    }

    public SchedulingStrategyBuilder withFixedFrequencyConfig(CronConfiguration cronConfiguration) {
        this.cronConfig = cronConfiguration;
        return this;
    }

    public SchedulingStrategyScheduler build() {
        if (SchedulingStrategy.FIXED_FREQUENCY.equals(strategy)) {
            if (fixedFrequencyConfig == null) {
                //"Scheduler 'fixedFrequencyConfig' property must be defined in the JSON definition when 'strategy' is FIXED_FREQUENCY"
                throw new ConfigurationException("Fixed frequency configuration");
            }
            return new SchedulingStrategySchedulerFixedFrequency(fixedFrequencyConfig);
        } else if (SchedulingStrategy.CRON.equals(strategy)) {
            if (cronConfig == null) {
                //"Scheduler 'cronConfig' property must be defined in the JSON definition when 'strategy' is CRON"
                throw new ConfigurationException("Cron configuration");
            }
            return new SchedulingStrategySchedulerCron(cronConfig);
        } else {
            // Scheduler 'strategy' value=['%s'] is not valid.
            throw new ConfigurationException("Scheduling strategy :  " + strategy + " not valid");
        }
    }

    public static SchedulingStrategyBuilder get(SchedulingStrategy schedulingStrategy) {
        return new SchedulingStrategyBuilder(schedulingStrategy);
    }
}
