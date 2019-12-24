package com.reedelk.scheduler.component;


import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.When;
import com.reedelk.runtime.api.component.AbstractInbound;
import com.reedelk.scheduler.commons.SchedulerJob;
import com.reedelk.scheduler.commons.SchedulingStrategyBuilder;
import com.reedelk.scheduler.configuration.CronConfiguration;
import com.reedelk.scheduler.configuration.FixedFrequencyConfiguration;
import com.reedelk.scheduler.configuration.SchedulingStrategy;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Scheduler")
@Component(service = Scheduler.class, scope = PROTOTYPE)
public class Scheduler extends AbstractInbound {

    @Property("Scheduling Strategy")
    @Default("FIXED_FREQUENCY")
    private SchedulingStrategy strategy;

    @Property("Fixed Frequency Configuration")
    @When(propertyName = "strategy", propertyValue = "FIXED_FREQUENCY")
    private FixedFrequencyConfiguration fixedFrequencyConfig;

    @Property("Cron Configuration")
    @When(propertyName = "strategy", propertyValue = "CRON")
    private CronConfiguration cronConfig;

    private SchedulerJob job;

    @Override
    public void onStart() {
        job = SchedulingStrategyBuilder.get(strategy)
                .withFixedFrequencyConfig(cronConfig)
                .withFixedFrequencyConfig(fixedFrequencyConfig)
                .build()
                .schedule(this);
    }

    @Override
    public void onShutdown() {
        if (job != null) {
            job.dispose();
        }
    }

    public void setStrategy(SchedulingStrategy strategy) {
        this.strategy = strategy;
    }

    public void setFixedFrequencyConfig(FixedFrequencyConfiguration fixedFrequencyConfig) {
        this.fixedFrequencyConfig = fixedFrequencyConfig;
    }

    public void setCronConfig(CronConfiguration cronConfig) {
        this.cronConfig = cronConfig;
    }
}
