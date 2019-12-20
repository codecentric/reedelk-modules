package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.esb.module.Module;
import com.reedelk.esb.services.resource.ResourceLoader;
import com.reedelk.runtime.api.resource.ResourceDynamic;
import com.reedelk.runtime.api.resource.ResourceNotFound;
import org.reactivestreams.Publisher;

import java.util.Collection;
import java.util.Optional;

import static com.reedelk.esb.commons.Messages.Resource.RESOURCE_DYNAMIC_NOT_FOUND;

public class ProxyResourceDynamic extends ResourceDynamic {

    private final Collection<ResourceLoader> resourceLoader;
    private final Module module;

    public ProxyResourceDynamic(ResourceDynamic original, Collection<ResourceLoader> resourceLoader, Module module) {
        super(original);
        this.module = module;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Publisher<byte[]> data(String evaluatedPath) {
        return resourceLoader.stream()
                .filter(loader -> loader.getResourceFilePath().endsWith(evaluatedPath))
                .findFirst()
                .flatMap(loader -> Optional.of(loader.body()))
                .orElseThrow(() -> {
                    // The file at the given path was not found in the Module bundle.
                    String message = RESOURCE_DYNAMIC_NOT_FOUND.format(evaluatedPath, value(), module.id(), module.name());
                    return new ResourceNotFound(message);
                });
    }
}