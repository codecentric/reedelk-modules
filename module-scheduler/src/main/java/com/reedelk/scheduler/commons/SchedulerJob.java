package com.reedelk.scheduler.commons;

import org.quartz.JobKey;
import org.quartz.SchedulerException;

public class SchedulerJob {

    private final JobKey jobKey;

    SchedulerJob(JobKey jobKey) {
        this.jobKey = jobKey;
    }

    public void dispose() {
        try {
            SchedulerProvider.getInstance().get().deleteJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
