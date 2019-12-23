package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.component.InboundEventListener;

public interface SchedulingStrategyScheduler {

    SchedulerJob schedule(InboundEventListener listener);
}
