package com.reedelk.esb.lifecycle;

import com.reedelk.esb.exception.FlowStopException;
import com.reedelk.esb.flow.Flow;
import com.reedelk.esb.module.Module;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

import static com.reedelk.esb.commons.Messages.Flow.STOP_ERROR;
import static com.reedelk.esb.commons.Messages.Flow.STOP_ERROR_WITH_TITLE;
import static com.reedelk.esb.module.state.ModuleState.STARTED;
import static com.reedelk.runtime.api.commons.StringUtils.EMPTY;

public class StopModuleAndReleaseReferences extends AbstractStep<Module, Module> {

    private static final Logger logger = LoggerFactory.getLogger(StopModuleAndReleaseReferences.class);

    @Override
    public Module run(Module module) {

        // This step is applicable only to Started Modules.
        if (module.state() != STARTED) return module;

        Collection<Flow> flows = module.flows();
        Collection<Exception> exceptions = new HashSet<>();
        for (Flow flow : flows) {
            try {
                flow.stopIfStarted();
            } catch (Exception exception) {
                String message = flow.getFlowTitle()
                        .map(flowTitle -> STOP_ERROR_WITH_TITLE.format(flow.getFlowId(), flowTitle))
                        .orElse(STOP_ERROR.format(flow.getFlowId()));
                FlowStopException stopException = new FlowStopException(message, exception);
                logger.error(EMPTY, stopException);

                exceptions.add(stopException);
            }
        }

        // Transition to STOPPED
        module.stop(flows);

        // Release references (including OSGi service/instances)
        Bundle bundle = bundle();
        flows.forEach(flow -> flow.releaseReferences(bundle));

        if (!exceptions.isEmpty()) {
            // Transition to ERROR
            module.error(exceptions);
        } else {
            // Transition to RESOLVED
            Collection<String> resolvedComponents = module.resolvedComponents();
            module.resolve(resolvedComponents);
        }

        return module;
    }
}
