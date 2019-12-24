package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.exception.ESBException;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.reedelk.scheduler.commons.Messages.Scheduler.*;

public class SchedulerProvider {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerProvider.class);

    private static Scheduler quartzScheduler;
    static {
        try {
            quartzScheduler = new StdSchedulerFactory().getScheduler();
            quartzScheduler.start();
        } catch (SchedulerException exception) {
            String message = ERROR_QUARTZ_SCHEDULER_INIT.format(exception.getMessage());
            throw new ESBException(message, exception);
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
            } catch (SchedulerException exception) {
                String message = ERROR_QUARTZ_SCHEDULER_DISPOSE.format(exception.getMessage());
                logger.warn(message, exception);
            }
        }
    }

    private SchedulerProvider() {
    }

    void deleteJob(JobKey jobKey) {
        try {
            quartzScheduler.checkExists(jobKey);
            quartzScheduler.deleteJob(jobKey);
        } catch (SchedulerException exception) {
            String message = ERROR_DELETE_JOB.format(jobKey.toString(), exception.getMessage());
            logger.warn(message, exception);
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
            String message = ERROR_SCHEDULE_JOB.format(jobID, exception.getMessage());
            throw new ESBException(message, exception);
        }
    }

    private Optional<SchedulerContext> getContext() {
        try {
            return Optional.ofNullable(quartzScheduler.getContext());
        } catch (SchedulerException exception) {
            String message = ERROR_QUARTZ_CONTEXT.format(exception.getMessage());
            logger.warn(message, exception);
            return Optional.empty();
        }
    }
}
