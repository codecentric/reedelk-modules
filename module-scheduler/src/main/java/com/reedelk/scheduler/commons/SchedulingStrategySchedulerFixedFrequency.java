package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.scheduler.configuration.FixedFrequencyConfiguration;
import com.reedelk.scheduler.configuration.TimeUnit;
import org.quartz.*;

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

        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(applyTimeUnit(simpleSchedule())
                        .repeatForever())
                .startAt(startDate())
                .build();

        scheduleJob(listener, job, trigger);

        return new SchedulerJob(job.getKey());
    }

    void scheduleJob(InboundEventListener listener, JobDetail job, Trigger trigger) {
        SchedulerProvider.scheduler().scheduleJob(listener, job, trigger);
    }

    Date startDate() {
        int delay = configuration.getDelay();
        TimeUnit timeUnit = configuration.getTimeUnit();
        long delayToAddInMs;
        if (TimeUnit.HOURS.equals(timeUnit)) {
            delayToAddInMs = java.util.concurrent.TimeUnit.HOURS.toMillis(delay);
        } else if (TimeUnit.MINUTES.equals(timeUnit)) {
            delayToAddInMs = java.util.concurrent.TimeUnit.MINUTES.toMillis(delay);
        } else if (TimeUnit.SECONDS.equals(timeUnit)) {
            delayToAddInMs = java.util.concurrent.TimeUnit.SECONDS.toMillis(delay);
        } else {
            // DEFAULT (Milliseconds)
            delayToAddInMs = delay;
        }
        return new Date(new Date().getTime() + delayToAddInMs);
    }

    SimpleScheduleBuilder applyTimeUnit(SimpleScheduleBuilder simpleSchedule) {
        int period = configuration.getPeriod();
        TimeUnit timeUnit = configuration.getTimeUnit();
        if (TimeUnit.HOURS.equals(timeUnit)) {
            return simpleSchedule.withIntervalInHours(period);
        } else if (TimeUnit.MINUTES.equals(timeUnit)) {
            return simpleSchedule.withIntervalInMinutes(period);
        } else if (TimeUnit.SECONDS.equals(timeUnit)) {
            return simpleSchedule.withIntervalInSeconds(period);
        } else {
            // DEFAULT (Milliseconds)
            return simpleSchedule.withIntervalInMilliseconds(period);
        }
    }
}
