package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.esb.module.DeserializedModule1;
import com.reedelk.esb.module.Module;
import com.reedelk.runtime.api.commons.ByteArrayStream;
import com.reedelk.runtime.api.resource.ResourceBinary;
import com.reedelk.runtime.api.resource.ResourceDynamic;
import com.reedelk.runtime.api.resource.ResourceNotFound;
import com.reedelk.runtime.api.resource.ResourceText;
import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.commons.TypeFactoryContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.reactivestreams.Publisher;

import java.util.Optional;

import static com.reedelk.esb.commons.Messages.Deserializer.RESOURCE_SOURCE_NOT_FOUND;

public class ResourceResolverDecorator implements TypeFactory {

    private final Module module;
    private final TypeFactory delegate;
    private final DeserializedModule1 deserializedModule1;

    public ResourceResolverDecorator(TypeFactory delegate, DeserializedModule1 deserializedModule1, Module module) {
        this.module = module;
        this.delegate = delegate;
        this.deserializedModule1 = deserializedModule1;
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
            return (T) new ProxyResourceDynamic((ResourceDynamic) result, deserializedModule1.getResources(), module);
        }

        return result;
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONArray jsonArray, int index, TypeFactoryContext context) {
        return delegate.create(expectedClass, jsonArray, index, context);
    }

    private ResourceText loadResourceText(ResourceText resource) {
        return deserializedModule1.getResources()
                .stream()
                .filter(resourceLoader -> resourceLoader.getResourceFilePath().endsWith(resource.path()))
                .findFirst()
                .flatMap(resourceLoader -> {
                    Publisher<byte[]> byteArrayStream = resourceLoader.body();
                    Publisher<String> stringStream = ByteArrayStream.asStringStream(byteArrayStream);
                    return Optional.of(new ProxyResourceText(resource, stringStream));
                })
                .orElseThrow(() -> new ResourceNotFound(RESOURCE_SOURCE_NOT_FOUND.format(resource.path())));
    }

    private ResourceBinary loadResourceBinary(ResourceBinary resource) {
        return deserializedModule1.getResources()
                .stream()
                .filter(resourceLoader -> resourceLoader.getResourceFilePath().endsWith(resource.path()))
                .findFirst()
                .flatMap(resourceLoader -> {
                    Publisher<byte[]> byteArrayStream = resourceLoader.body();
                    return Optional.of(new ProxyResourceBinary(resource, byteArrayStream));
                })
                .orElseThrow(() -> new ResourceNotFound(RESOURCE_SOURCE_NOT_FOUND.format(resource.path())));
    }
}