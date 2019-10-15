package com.reedelk.esb.services.module;

import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.state.ModuleState;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import static com.reedelk.esb.module.state.ModuleState.*;
import static java.util.stream.Collectors.toList;

public class ModulesMapper {

    public com.reedelk.runtime.system.api.Module map(Module module) {
        ModuleState state = module.state();
        com.reedelk.runtime.system.api.Module moduleDto = new com.reedelk.runtime.system.api.Module();
        moduleDto.setState(state.name());
        moduleDto.setName(module.name());
        moduleDto.setModuleId(module.id());
        moduleDto.setVersion(module.version());
        moduleDto.setModuleFilePath(module.filePath());

        if (STARTED == state || STOPPED == state || RESOLVED == state) {
            moduleDto.setResolvedComponents(module.resolvedComponents());
        }
        if (UNRESOLVED == state) {
            moduleDto.setUnresolvedComponents(module.unresolvedComponents());
            moduleDto.setResolvedComponents(module.resolvedComponents());
        }
        if (ERROR == state) {
            moduleDto.setErrors(serializeExceptions(module.errors()));
        }
        return moduleDto;
    }

    private Collection<String> serializeExceptions(Collection<Exception> exceptions) {
        return exceptions
                .stream()
                .map(this::getStackTrace)
                .collect(toList());
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
