package com.reedelk.scheduler.configuration;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = FixedFrequencyConfiguration.class, scope = PROTOTYPE)
public class FixedFrequencyConfiguration implements Implementor {

    @Property("Frequency")
    @Default("1000")
    private long period;

    @Property("Start delay")
    @Default("0")
    private long delay;

    @Property("Time unit")
    @Default("MILLISECONDS")
    private TimeUnit timeUnit;

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
