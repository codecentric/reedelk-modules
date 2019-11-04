package com.reedelk.scheduler.component;


import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.AbstractInbound;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.DefaultMessageAttributes;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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

    private ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

    @Property("Delay")
    @Default("0")
    private long delay;

    @Property("Period")
    @Default("1000")
    private long period;

    private ScheduledFuture<?> scheduledFuture;

    public void onStart() {
        this.scheduledFuture =
                service.scheduleAtFixedRate(command(), delay, period, MILLISECONDS);
    }

    public void onShutdown() {
        scheduledFuture.cancel(false);
        service.shutdown();
        try {
            service.awaitTermination(1, SECONDS);
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

    private Runnable command() {
        return () -> {

            Map<String, Serializable> attributesMap = new HashMap<>();
            attributesMap.put(SchedulerAttribute.firedAt(), System.currentTimeMillis());
            DefaultMessageAttributes attributes = new DefaultMessageAttributes(attributesMap);

            Message emptyMessage = MessageBuilder.get()
                    .attributes(attributes)
                    .empty()
                    .build();

            onEvent(emptyMessage, new OnResult() {
                @Override
                public void onError(Throwable throwable, FlowContext flowContext) {
                    // we catch any exception, we want to keep the scheduler to run.
                    // (otherwise by default it stops its execution)
                    logger.error("scheduler", throwable);
                }
            });
        };
    }
}
