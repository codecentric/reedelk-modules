package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.esb.exception.FileNotFoundException;
import com.reedelk.esb.module.Module;
import com.reedelk.esb.services.resource.ResourceLoader;
import com.reedelk.runtime.api.resource.ResourceDynamic;
import org.reactivestreams.Publisher;

import java.util.Collection;
import java.util.Optional;

import static com.reedelk.esb.commons.Messages.Module.FILE_NOT_FOUND_ERROR;

public class ProxyResourceDynamic extends ResourceDynamic {

    private final Collection<ResourceLoader> resourceLoader;
    private final Module module;

    public ProxyResourceDynamic(ResourceDynamic original, Collection<ResourceLoader> resourceLoader, Module module) {
        super(original.body(), original.getContext());
        this.resourceLoader = resourceLoader;
        this.module = module;
    }

    @Override
    public Publisher<byte[]> data(String evaluatedPath) {
        return resourceLoader.stream()
                .filter(loader -> loader.getResourceFilePath().endsWith(evaluatedPath))
                .findFirst()
                .flatMap(loader -> Optional.of(loader.body()))
                .orElseThrow(() -> {
                    // The file at the given path was not found in the Module bundle.
                    String message = FILE_NOT_FOUND_ERROR.format(evaluatedPath, module.id(), module.name());
                    throw new FileNotFoundException(message);
                });
    }
}