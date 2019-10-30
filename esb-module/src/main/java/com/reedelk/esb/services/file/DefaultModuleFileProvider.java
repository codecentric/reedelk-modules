package com.reedelk.esb.services.file;

import com.reedelk.esb.commons.Messages;
import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModulesManager;
import com.reedelk.runtime.api.commons.StackTraceUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.exception.ModuleFileNotFoundException;
import com.reedelk.runtime.api.file.ModuleFileProvider;
import com.reedelk.runtime.api.file.ModuleId;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import static com.reedelk.esb.commons.Preconditions.checkNotNull;

public class DefaultModuleFileProvider implements ModuleFileProvider {

    private static final int FILE_READ_BUFFER_SIZE = 4096;

    private final BundleContext context;
    private final ModulesManager modulesManager;

    public DefaultModuleFileProvider(BundleContext context, ModulesManager modulesManager) {
        checkNotNull(context, "context");
        checkNotNull(modulesManager, "modulesManager");
        this.context = context;
        this.modulesManager = modulesManager;
    }

    @Override
    public Publisher<byte[]> findBy(ModuleId moduleId, String path) {
        Bundle bundle = context.getBundle(moduleId.get());
        Module module = modulesManager.getModuleById(moduleId.get());

        try {
            Enumeration<URL> resources = bundle.getResources(path);

            if (!resources.hasMoreElements()) {
                // The file at the given path was not found in the Module bundle.
                String message = Messages.Module.FILE_NOT_FOUND_ERROR.format(
                        path,
                        module.id(),
                        module.name(),
                        module.version(),
                        module.filePath());
                throw new ModuleFileNotFoundException(message);
            }

            URL targetFileURL = resources.nextElement();
            return streamFromURL(targetFileURL);

        } catch (IOException exception) {
            String rootCauseMessage = StackTraceUtils.rootCauseMessageOf(exception);
            String message = Messages.Module.FILE_FIND_ERROR.format(
                    path,
                    module.id(),
                    module.name(),
                    module.version(),
                    module.filePath(),
                    rootCauseMessage);
            throw new ESBException(message, exception);
        }
    }

    private static Publisher<byte[]> streamFromURL(URL target) {
        return Flux.create(fluxSink -> {
            try (InputStream inputStream = target.openStream()) {
                byte[] byteChunk = new byte[FILE_READ_BUFFER_SIZE];
                int n;
                while ((n = inputStream.read(byteChunk)) > 0) {
                    byte[] chunk = new byte[n];
                    System.arraycopy(byteChunk, 0, chunk, 0, n);
                    fluxSink.next(chunk);
                }
                fluxSink.complete();
            } catch (IOException e) {
                fluxSink.error(e);
            }
        });
    }
}
