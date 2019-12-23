package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.exception.ESBException;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerProvider {

    private static final Scheduler QUARTZ_SCHEDULER;
    static {
        try {
            QUARTZ_SCHEDULER = new StdSchedulerFactory().getScheduler();
            QUARTZ_SCHEDULER.start();
        } catch (SchedulerException e) {
            throw new ESBException();
        }
    }

    private SchedulerProvider() {
    }

    private static class SchedulerProviderHelper {
        private static final SchedulerProvider INSTANCE = new SchedulerProvider();
    }

    public static SchedulerProvider getInstance() {
        return SchedulerProviderHelper.INSTANCE;
    }

    public static void dispose() {
        if (QUARTZ_SCHEDULER != null) {
            try {
                if (!QUARTZ_SCHEDULER.isShutdown()) {
                    QUARTZ_SCHEDULER.shutdown();
                }
            } catch (SchedulerException e) {
                // TODO: Just log this exceptioin there is nothing we can do to recover here
            }
        }
    }

    public Scheduler get() {
        return QUARTZ_SCHEDULER;
    }

    public void scheduleJob(InboundEventListener listener, JobDetail job, Trigger trigger) {
        try {
            QUARTZ_SCHEDULER.getContext().put(job.getKey().toString(), this);
            QUARTZ_SCHEDULER.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            // TODO: Do cleanup: remove from context the key and remove the schedule job if  present.
            // TODO: And then REthrow the esb exception.
        }
    }
}
