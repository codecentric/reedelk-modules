package com.reedelk.scheduler.component;


import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.AbstractInbound;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.Message;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Scheduler")
@Component(service = Scheduler.class, scope = PROTOTYPE)
public class Scheduler extends AbstractInbound {

    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    private ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(1);

    @Property("Delay")
    @Default("0")
    private long delay;

    @Property("Period")
    @Default("1000")
    private long period;

    private ScheduledFuture<?> scheduledFuture;

    public void onStart() {
        this.scheduledFuture = scheduledService.scheduleAtFixedRate(() -> {
            try {
                Message emptyMessage = new Message();
                onEvent(emptyMessage, new OnResult() {
                });
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

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

}
