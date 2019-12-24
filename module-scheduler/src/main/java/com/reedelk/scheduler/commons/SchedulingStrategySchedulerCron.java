package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.scheduler.configuration.CronConfiguration;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.TimeZone;

import static org.quartz.CronScheduleBuilder.cronSchedule;

class SchedulingStrategySchedulerCron implements SchedulingStrategyScheduler {

    private final CronConfiguration configuration;

    SchedulingStrategySchedulerCron(CronConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SchedulerJob schedule(InboundEventListener listener) {
        JobDetail job = JobBuilder.newJob(ExecuteFlowJob.class).build();

        String expression = configuration.getExpression();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(cronSchedule(expression).inTimeZone(getTimeZoneOrDefault()))
                .build();

        scheduleJob(listener, job, trigger);
        return new SchedulerJob(job.getKey());
    }

    void scheduleJob(InboundEventListener listener, JobDetail job, Trigger trigger) {
        SchedulerProvider.scheduler().scheduleJob(listener, job, trigger);
    }

    TimeZone getTimeZoneOrDefault() {
        String timeZone = configuration.getTimeZone();
        if (StringUtils.isBlank(timeZone)) {
            return TimeZone.getDefault();
        } else {
            return TimeZone.getTimeZone(timeZone);
        }
    }
}
