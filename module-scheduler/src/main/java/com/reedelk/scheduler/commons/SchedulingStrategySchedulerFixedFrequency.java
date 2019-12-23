package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.scheduler.configuration.FixedFrequencyConfiguration;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

class SchedulingStrategySchedulerFixedFrequency implements SchedulingStrategyScheduler {

    private final FixedFrequencyConfiguration configuration;

    SchedulingStrategySchedulerFixedFrequency(FixedFrequencyConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SchedulerJob schedule(InboundEventListener listener) {
        JobDetail job = JobBuilder.newJob(ExecuteFlowJob.class).build();

        long period = configuration.getPeriod();
        long delay = configuration.getDelay();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(simpleSchedule()
                        .withIntervalInMilliseconds(period)
                        .repeatForever())
                .startAt(new Date(new Date().getTime() + delay))
                .build();
        SchedulerProvider.getInstance().scheduleJob(listener, job, trigger);
        return new SchedulerJob(job.getKey());
    }
}
