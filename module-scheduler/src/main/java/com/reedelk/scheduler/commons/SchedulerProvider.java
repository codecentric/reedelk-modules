package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.exception.ESBException;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SchedulerProvider {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerProvider.class);

    private static Scheduler quartzScheduler;
    static {
        try {
            quartzScheduler = new StdSchedulerFactory().getScheduler();
            quartzScheduler.start();
        } catch (SchedulerException e) {
            throw new ESBException();
        }
    }

    private static class SchedulerProviderHelper {
        private static final SchedulerProvider INSTANCE = new SchedulerProvider();
    }

    static SchedulerProvider scheduler() {
        return SchedulerProviderHelper.INSTANCE;
    }

    public static void dispose() {
        if (quartzScheduler != null) {
            try {
                if (!quartzScheduler.isShutdown()) {
                    quartzScheduler.shutdown();
                }
                quartzScheduler = null;
            } catch (SchedulerException e) {
                logger.warn("Could not dispose quartz scheduler: " + e.getMessage(), e);
            }
        }
    }

    private SchedulerProvider() {
    }

    void deleteJob(JobKey jobKey) {
        try {
            quartzScheduler.checkExists(jobKey);
            quartzScheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            logger.warn("Could not delete job with ID: " + jobKey.toString() + ": "+ e.getMessage(), e);
        }
    }

    void scheduleJob(InboundEventListener listener, JobDetail job, Trigger trigger) {
        String jobID = job.getKey().toString();
        try {
            quartzScheduler.getContext().put(jobID, listener);
            quartzScheduler.scheduleJob(job, trigger);
        } catch (SchedulerException exception) {
            // Cleanup
            getContext().ifPresent(schedulerContext -> schedulerContext.remove(jobID));
            deleteJob(job.getKey());
            String message = String.format("Could not schedule job with id=%s", jobID);
            throw new ESBException(message, exception);
        }
    }

    private Optional<SchedulerContext> getContext() {
        try {
            return Optional.ofNullable(quartzScheduler.getContext());
        } catch (SchedulerException e) {
            logger.warn("Could not get quartz context: " + e.getMessage(), e);
            return Optional.empty();
        }
    }
}
