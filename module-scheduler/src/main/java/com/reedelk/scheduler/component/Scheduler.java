package com.reedelk.scheduler.component;


import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.When;
import com.reedelk.runtime.api.component.AbstractInbound;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.scheduler.commons.ExecuteFlowJob;
import com.reedelk.scheduler.commons.TimeZoneUtils;
import com.reedelk.scheduler.configuration.CronConfiguration;
import com.reedelk.scheduler.configuration.FixedFrequencyConfiguration;
import com.reedelk.scheduler.configuration.SchedulingStrategy;
import com.reedelk.scheduler.commons.SchedulerProvider;
import org.osgi.service.component.annotations.Component;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@ESBComponent("Scheduler")
@Component(service = Scheduler.class, scope = PROTOTYPE)
public class Scheduler extends AbstractInbound {

    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    @Property("Scheduling Strategy")
    @Default("FIXED_FREQUENCY")
    private SchedulingStrategy strategy;

    @Property("Fixed Frequency")
    @When(propertyName = "strategy", propertyValue = "FIXED_FREQUENCY")
    private FixedFrequencyConfiguration fixedFrequency;

    @Property("Cron")
    @When(propertyName = "strategy", propertyValue = "CRON")
    private CronConfiguration cron;

    private JobKey startedJobKey;

    @Override
    public void onStart() {

        JobDetail job = JobBuilder.newJob(ExecuteFlowJob.class)
                .build();
        startedJobKey = job.getKey();

        Trigger trigger;

        if (strategy == SchedulingStrategy.FIXED_FREQUENCY) {
            long period = fixedFrequency.getPeriod();
            long delay = fixedFrequency.getDelay();
            trigger = TriggerBuilder.newTrigger()
                    .withSchedule(simpleSchedule()
                            .withIntervalInMilliseconds(period)
                            .repeatForever())
                    .startAt(new Date(new Date().getTime() + delay))
                    .build();
        } else if (strategy == SchedulingStrategy.CRON) {
            String expression = cron.getExpression();
            String timeZone = cron.getTimeZone();
            trigger = TriggerBuilder.newTrigger()
                    .withSchedule(cronSchedule(expression)
                            .inTimeZone(TimeZoneUtils.getOrDefault(timeZone)))
                    .build();
        } else {
            throw new ESBException("Erro");
        }

        SchedulerProvider.getInstance().scheduleJob(this, job, trigger);
    }

    @Override
    public void onShutdown() {
        if (startedJobKey != null) {
            try {
                SchedulerProvider.getInstance().get().deleteJob(startedJobKey);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }

    public void setStrategy(SchedulingStrategy strategy) {
        this.strategy = strategy;
    }

    public void setFixedFrequency(FixedFrequencyConfiguration fixedFrequency) {
        this.fixedFrequency = fixedFrequency;
    }

    public void setCron(CronConfiguration cron) {
        this.cron = cron;
    }
}
