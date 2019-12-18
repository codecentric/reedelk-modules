package com.reedelk.esb.services.resource;

import com.reedelk.esb.exception.FileNotFoundException;
import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.runtime.api.commons.ModuleId;
import com.reedelk.runtime.api.commons.StackTraceUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.resource.Resource;
import com.reedelk.runtime.api.resource.ResourceProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import static com.reedelk.esb.commons.Messages.Module.FILE_FIND_IO_ERROR;
import static com.reedelk.esb.commons.Messages.Module.FILE_NOT_FOUND_ERROR;

// TODO: Add a bunch of methods to return
//  bytes,
//  byte stream,
//  string,
//  string stream
public class DefaultResourceProvider implements ResourceProvider {

    private static final int DEFAULT_READ_BUFFER_SIZE = 65536; // TODO: Should be a configurable system property

    private final BundleContext context;
    private final ModulesManager modulesManager;

    public DefaultResourceProvider(BundleContext context, ModulesManager modulesManager) {
        this.context = context;
        this.modulesManager = modulesManager;
    }

    @Override
    public Publisher<byte[]> findResourceBy(ModuleId moduleId, String resourcePath, int bufferSize) {
        Bundle bundle = context.getBundle(moduleId.get());
        Module module = modulesManager.getModuleById(moduleId.get());

        try {
            Enumeration<URL> resources = bundle.getResources(resourcePath);

            if (resources == null || !resources.hasMoreElements()) {
                // The file at the given path was not found in the Module bundle.
                String message = FILE_NOT_FOUND_ERROR.format(
                        resourcePath,
                        module.id(),
                        module.name());
                throw new FileNotFoundException(message);
            }

            URL targetFileURL = resources.nextElement();
            return StreamFrom.url(targetFileURL, bufferSize);

        } catch (IOException exception) {
            String rootCauseMessage = StackTraceUtils.rootCauseMessageOf(exception);
            String message = FILE_FIND_IO_ERROR.format(
                    resourcePath,
                    module.id(),
                    module.name(),
                    rootCauseMessage);
            throw new ESBException(message, exception);
        }
    }

    @Override
    public Publisher<byte[]> findResourceBy(ModuleId moduleId, String resourcePath) {
        return findResourceBy(moduleId, resourcePath, DEFAULT_READ_BUFFER_SIZE);
    }

    @Override
    public Publisher<byte[]> findResourceBy(Resource resource) {
        return findResourceBy(resource.getContext().getModuleId(), resource.getResourcePath());
    }
}