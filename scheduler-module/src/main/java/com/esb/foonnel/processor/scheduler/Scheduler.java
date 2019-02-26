package com.esb.foonnel.processor.scheduler;


import com.esb.foonnel.api.component.AbstractInbound;
import com.esb.foonnel.api.message.Message;
import org.osgi.service.component.annotations.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = Scheduler.class, scope = PROTOTYPE)
public class Scheduler extends AbstractInbound {

    private ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(1);

    private long delay;
    private long period;

    private ScheduledFuture<?> scheduledFuture;

    public void onStart() {
        this.scheduledFuture = scheduledService.scheduleAtFixedRate(() -> {
            Message emptyMessage = new Message();
            onEvent(emptyMessage);

        }, delay, period, TimeUnit.MILLISECONDS);
    }

    public void onShutdown() {
        scheduledFuture.cancel(false);
        scheduledService.shutdown();
        try {
            scheduledService.awaitTermination(1, TimeUnit.SECONDS);
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
