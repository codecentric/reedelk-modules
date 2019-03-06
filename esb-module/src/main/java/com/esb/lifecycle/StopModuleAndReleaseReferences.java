package com.esb.lifecycle;

import com.esb.flow.Flow;
import com.esb.module.Module;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

import static com.esb.module.state.ModuleState.STARTED;
import static java.lang.String.format;

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
                exceptions.add(exception);
                logger.error(format("Error Stopping Flow with id=%s", flow.getFlowId()), exception);
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
