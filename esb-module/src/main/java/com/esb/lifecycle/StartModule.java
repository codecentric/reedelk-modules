package com.esb.lifecycle;


import com.esb.flow.Flow;
import com.esb.module.Module;
import com.esb.module.state.ModuleState;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

public class StartModule extends AbstractStep<Module, Module> {

    private static final Logger logger = LoggerFactory.getLogger(StartModule.class);

    @Override
    public Module run(Module module) {

        if (module.state() != ModuleState.STOPPED) return module;

        Collection<Flow> flows = module.flows();
        Collection<Exception> exceptions = new HashSet<>();

        for (Flow flow : flows) {
            try {
                flow.start();
            } catch (Exception exception) {
                logger.error("Start Flow", exception);
                exceptions.add(exception);
            }
        }

        if (!exceptions.isEmpty()) {
            // At least one exception has been thrown while starting one/many flows from the module.
            // Since errors where thrown onStart, flows might have not be completely started.
            // To give a chance to cleanup resources which might have been created during the
            // failed start attempt, we force the flow to stop and then we release any reference.
            flows.forEach(this::forceStop);

            // Release Flow references (including OSGi services)
            Bundle bundle = bundle();
            flows.forEach(flow -> flow.releaseReferences(bundle));

            // Transition to Error state
            module.error(exceptions);

        } else {
            module.start(flows);
        }

        return module;
    }

    private void forceStop(Flow flow) {
        try {
            flow.forceStop();
        } catch (Exception e) {
            logger.warn("Force stop", e);
        }
    }
}
