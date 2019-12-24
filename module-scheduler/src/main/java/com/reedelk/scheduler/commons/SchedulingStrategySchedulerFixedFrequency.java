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

        int period = configuration.getPeriod();
        int delay = configuration.getDelay();
        TimeUnit timeUnit = configuration.getTimeUnit();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(applyTimeUnit(simpleSchedule(), timeUnit, period)
                        .repeatForever())
                .startAt(new Date(new Date().getTime() + delay))
                .build();
        SchedulerProvider.scheduler().scheduleJob(listener, job, trigger);
        return new SchedulerJob(job.getKey());
    }

    private SimpleScheduleBuilder applyTimeUnit(SimpleScheduleBuilder simpleSchedule, TimeUnit timeUnit, int period) {
        if (TimeUnit.MILLISECONDS.equals(timeUnit)) {
         return simpleSchedule.withIntervalInMilliseconds(period);
        } else if (TimeUnit.HOURS.equals(timeUnit)) {
            return simpleSchedule.withIntervalInHours(period);
        } else if (TimeUnit.MINUTES.equals(timeUnit)) {
            return simpleSchedule.withIntervalInMinutes(period);
        } else if (TimeUnit.SECONDS.equals(timeUnit)) {
            return simpleSchedule.withIntervalInSeconds(period);
        } else {
            // DEFAULT
            return simpleSchedule.withIntervalInMilliseconds(period);
        }
    }
}
