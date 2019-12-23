package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.scheduler.configuration.CronConfiguration;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

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
        String timeZone = configuration.getTimeZone();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(cronSchedule(expression)
                        .inTimeZone(TimeZoneUtils.getOrDefault(timeZone)))
                .build();
        SchedulerProvider.getInstance().scheduleJob(listener, job, trigger);
        return new SchedulerJob(job.getKey());
    }
}
