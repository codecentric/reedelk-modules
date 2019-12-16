package com.reedelk.esb.services.file;

import com.reedelk.esb.exception.FileNotFoundException;
import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.runtime.api.commons.StackTraceUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.file.ModuleFileProvider;
import com.reedelk.runtime.api.file.ModuleId;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import static com.reedelk.esb.commons.Messages.Module.FILE_FIND_IO_ERROR;
import static com.reedelk.esb.commons.Messages.Module.FILE_NOT_FOUND_ERROR;

public class DefaultModuleFileProvider implements ModuleFileProvider {

    private final BundleContext context;
    private final ModulesManager modulesManager;

    public DefaultModuleFileProvider(BundleContext context, ModulesManager modulesManager) {
        this.context = context;
        this.modulesManager = modulesManager;
    }

    @Override
    public Publisher<byte[]> findBy(ModuleId moduleId, String path, int bufferSize) {
        Bundle bundle = context.getBundle(moduleId.get());
        Module module = modulesManager.getModuleById(moduleId.get());

        try {
            Enumeration<URL> resources = bundle.getResources(path);

            if (resources == null || !resources.hasMoreElements()) {
                // The file at the given path was not found in the Module bundle.
                String message = FILE_NOT_FOUND_ERROR.format(
                        path,
                        module.id(),
                        module.name());
                throw new FileNotFoundException(message);
            }

            URL targetFileURL = resources.nextElement();
            return StreamFrom.url(targetFileURL, bufferSize);

        } catch (IOException exception) {
            String rootCauseMessage = StackTraceUtils.rootCauseMessageOf(exception);
            String message = FILE_FIND_IO_ERROR.format(
                    path,
                    module.id(),
                    module.name(),
                    rootCauseMessage);
            throw new ESBException(message, exception);
        }
    }
}
