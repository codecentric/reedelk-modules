package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.exception.ESBException;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerProvider {

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
                // TODO: Just log this exceptioin there is nothing we can do to recover here
            }
        }
    }

    private SchedulerProvider() {
    }

    void deleteJob(JobKey jobKey) {
        try {
            quartzScheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            // TODO: Adjust this
        }
    }

    void scheduleJob(InboundEventListener listener, JobDetail job, Trigger trigger) {
        try {
            quartzScheduler.getContext().put(job.getKey().toString(), listener);
            quartzScheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            // TODO: Do cleanup: remove from context the key and remove the schedule job if  present.
            // TODO: And then REthrow the esb exception.
        }
    }
}
