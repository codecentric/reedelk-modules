package com.esb.scheduler.component;


import com.esb.api.component.AbstractInbound;
import com.esb.api.message.Message;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.*;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = Scheduler.class, scope = PROTOTYPE)
public class Scheduler extends AbstractInbound {

    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    private ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(1);

    private long delay;
    private long period;

    private ScheduledFuture<?> scheduledFuture;

    public void onStart() {
        this.scheduledFuture = scheduledService.scheduleAtFixedRate(() -> {
            try {
                Message emptyMessage = new Message();
                onEvent(emptyMessage);
            } catch (Exception e) {
                // we catch any exception, we want to keep the scheduler to run.
                // (otherwise by default it stops its execution)
                logger.error("scheduler", e);
            }

        }, delay, period, MILLISECONDS);
    }

    public void onShutdown() {
        scheduledFuture.cancel(false);
        scheduledService.shutdown();
        try {
            scheduledService.awaitTermination(1, SECONDS);
        } catch (InterruptedException e) {
            // nothing to do
        }
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

}
