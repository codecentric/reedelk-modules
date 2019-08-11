package com.reedelk.esb.lifecycle;

import com.reedelk.esb.component.ComponentRegistry;
import com.reedelk.esb.module.ModulesManager;
import org.osgi.framework.Bundle;

public interface Step<I, O> {

    O run(I input);

    Bundle bundle();

    void bundle(Bundle bundle);

    ModulesManager modulesManager();

    void modulesManager(ModulesManager modulesManager);

    ComponentRegistry componentRegistry();

    void componentRegistry(ComponentRegistry componentRegistry);
}
