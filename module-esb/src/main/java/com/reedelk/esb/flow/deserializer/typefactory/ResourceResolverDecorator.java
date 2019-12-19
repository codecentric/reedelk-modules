package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.esb.exception.FileNotFoundException;
import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.deserializer.ResourceLoader;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.resource.ResourceBinary;
import com.reedelk.runtime.api.resource.ResourceDynamic;
import com.reedelk.runtime.api.resource.ResourceText;
import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.commons.TypeFactoryContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Optional;

import static com.reedelk.esb.commons.Messages.Deserializer.RESOURCE_SOURCE_NOT_FOUND;
import static com.reedelk.esb.commons.Messages.Module.FILE_NOT_FOUND_ERROR;

public class ResourceResolverDecorator implements TypeFactory {

    private final Module module;
    private final TypeFactory delegate;
    private final DeserializedModule deserializedModule;

    public ResourceResolverDecorator(TypeFactory delegate, DeserializedModule deserializedModule, Module module) {
        this.module = module;
        this.delegate = delegate;
        this.deserializedModule = deserializedModule;
    }

    @Override
    public boolean isPrimitive(Class<?> clazz) {
        return delegate.isPrimitive(clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Class<T> expectedClass, JSONObject jsonObject, String propertyName, TypeFactoryContext context) {
        T result = delegate.create(expectedClass, jsonObject, propertyName, context);
        if (result instanceof ResourceText) {
            return (T) loadResourceText((ResourceText) result);
        }
        if (result instanceof ResourceBinary) {
            return (T) loadResourceBinary((ResourceBinary) result);
        }
        if  (result instanceof ResourceDynamic) {
            return (T) loadDynamicResource((ResourceDynamic) result);
        }
        return result;
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONArray jsonArray, int index, TypeFactoryContext context) {
        return delegate.create(expectedClass, jsonArray, index, context);
    }

    private ResourceText loadResourceText(ResourceText resource) {
        return deserializedModule.getMetadataResources()
                .stream()
                .filter(resourceLoader -> resourceLoader.getResourceFilePath().endsWith(resource.getResourcePath()))
                .findFirst()
                .flatMap(resourceLoader -> Optional.of(new ResourceTextProxy(resource, resourceLoader.bodyAsString())))
                .orElseThrow(() -> new ESBException(RESOURCE_SOURCE_NOT_FOUND.format(resource.getResourcePath())));
    }

    private ResourceBinary loadResourceBinary(ResourceBinary resource) {
        return deserializedModule.getMetadataResources()
                .stream()
                .filter(resourceLoader -> resourceLoader.getResourceFilePath().endsWith(resource.getResourcePath()))
                .findFirst()
                .flatMap(resourceLoader -> Optional.of(new ResourceBinaryProxy(resource, resourceLoader.bodyAsBytes())))
                .orElseThrow(() -> new ESBException(RESOURCE_SOURCE_NOT_FOUND.format(resource.getResourcePath())));
    }


    private ResourceDynamic loadDynamicResource(ResourceDynamic resource) {
       return new ResourceDynamicProxy(resource, deserializedModule.getMetadataResources());
    }

    class ResourceDynamicProxy extends ResourceDynamic {

        private final Collection<ResourceLoader> resourceLoader;

        ResourceDynamicProxy(ResourceDynamic original, Collection<ResourceLoader> resourceLoader) {
            super(original.body(), original.getContext());
            this.resourceLoader = resourceLoader;
        }

        @Override
        public byte[] load(String evaluatedPath) {
            return resourceLoader.stream()
                    .filter(loader -> loader.getResourceFilePath().endsWith(evaluatedPath))
                    .findFirst()
                    .flatMap(loader -> Optional.of(loader.bodyAsBytes()))
                    .orElseThrow(() -> {
                        // The file at the given path was not found in the Module bundle.
                        String message = FILE_NOT_FOUND_ERROR.format(evaluatedPath, module.id(), module.name());
                        throw new FileNotFoundException(message);
                    });
        }
    }

    class ResourceTextProxy extends ResourceText {

        private final String data;

        ResourceTextProxy(ResourceText original, String data) {
            super(original.getResourcePath(), original.getContext());
            this.data = data;
        }

        @Override
        public String data() {
            return data;
        }
    }

    class ResourceBinaryProxy extends ResourceBinary {

        private final byte[] data;

        ResourceBinaryProxy(ResourceBinary original, byte[] data) {
            super(original.getResourcePath(), original.getContext());
            this.data = data;
        }

        @Override
        public byte[] data() {
            return data;
        }
    }
}