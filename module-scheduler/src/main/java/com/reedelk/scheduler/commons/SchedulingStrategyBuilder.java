package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.scheduler.configuration.CronConfiguration;
import com.reedelk.scheduler.configuration.FixedFrequencyConfiguration;
import com.reedelk.scheduler.configuration.SchedulingStrategy;

public class SchedulingStrategyBuilder {

    private SchedulingStrategy strategy;
    private CronConfiguration cronConfiguration;
    private FixedFrequencyConfiguration fixedFrequencyConfiguration;

    private SchedulingStrategyBuilder(SchedulingStrategy schedulingStrategy) {
        this.strategy = schedulingStrategy;
    }

    public SchedulingStrategyBuilder withConfig(FixedFrequencyConfiguration fixedFrequencyConfiguration) {
        this.fixedFrequencyConfiguration = fixedFrequencyConfiguration;
        return this;
    }

    public SchedulingStrategyBuilder withConfig(CronConfiguration cronConfiguration) {
        this.cronConfiguration = cronConfiguration;
        return this;
    }

    // TODO: Use same config exception as for the rest-module
    public SchedulingStrategyScheduler build() {
        if (SchedulingStrategy.FIXED_FREQUENCY.equals(strategy)) {
            if (fixedFrequencyConfiguration == null) {
                throw new ESBException("Fixed frequency configuration");
            }
            return new SchedulingStrategySchedulerFixedFrequency(fixedFrequencyConfiguration);
        } else if (SchedulingStrategy.CRON.equals(strategy)) {
            if (cronConfiguration == null) {
                throw new ESBException("Cron configuration");
            }
            return new SchedulingStrategySchedulerCron(cronConfiguration);
        } else {
            throw new ESBException("Scheduling strategy :  " + strategy + " not valid");
        }
    }

    public static SchedulingStrategyBuilder get(SchedulingStrategy schedulingStrategy) {
        return new SchedulingStrategyBuilder(schedulingStrategy);
    }
}
