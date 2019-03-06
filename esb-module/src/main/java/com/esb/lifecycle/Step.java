package com.esb.lifecycle;

import com.esb.component.ComponentRegistry;
import com.esb.module.ModulesManager;
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
