package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.component.InboundEventListener;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.DefaultMessageAttributes;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.scheduler.component.Scheduler;
import com.reedelk.scheduler.configuration.SchedulerAttribute;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.reedelk.scheduler.commons.Messages.Scheduler.ERROR_QUARTZ_CONTEXT;

public class ExecuteFlowJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteFlowJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Map<String, Serializable> attributesMap = new HashMap<>();
        attributesMap.put(SchedulerAttribute.firedAt(), jobExecutionContext.getFireTime().getTime());
        DefaultMessageAttributes attributes = new DefaultMessageAttributes(Scheduler.class, attributesMap);

        Message emptyMessage = MessageBuilder.get()
                .attributes(attributes)
                .empty()
                .build();


        SchedulerContext context;
        try {
            context = jobExecutionContext.getScheduler().getContext();
        } catch (SchedulerException exception) {
            String message = ERROR_QUARTZ_CONTEXT.format(exception.getMessage());
            logger.error(message, exception);
            throw new JobExecutionException(exception);
        }

        InboundEventListener inbound = (InboundEventListener) context.get(jobExecutionContext.getJobDetail().getKey().toString());
        inbound.onEvent(emptyMessage, new OnResult() {});
    }
}
