package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.exception.ConfigurationException;
import com.reedelk.scheduler.configuration.CronConfiguration;
import com.reedelk.scheduler.configuration.FixedFrequencyConfiguration;
import com.reedelk.scheduler.configuration.SchedulingStrategy;

import static com.reedelk.scheduler.commons.Messages.Scheduler.*;

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
                String message = ERROR_CONFIG_FIXED_FREQUENCY_MISSING.format(SchedulingStrategy.FIXED_FREQUENCY.toString());
                throw new ConfigurationException(message);
            }
            return new SchedulingStrategySchedulerFixedFrequency(fixedFrequencyConfig);
        } else if (SchedulingStrategy.CRON.equals(strategy)) {
            if (cronConfig == null) {
                String message = ERROR_CONFIG_CRON_MISSING.format(SchedulingStrategy.CRON.toString());
                throw new ConfigurationException(message);
            }
            return new SchedulingStrategySchedulerCron(cronConfig);
        } else {
            String message = ERROR_CONFIG_SCHEDULING_STRATEGY.format(strategy);
            throw new ConfigurationException(message);
        }
    }

    public static SchedulingStrategyBuilder get(SchedulingStrategy schedulingStrategy) {
        return new SchedulingStrategyBuilder(schedulingStrategy);
    }
}
