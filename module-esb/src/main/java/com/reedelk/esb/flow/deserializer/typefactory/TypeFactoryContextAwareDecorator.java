package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.runtime.api.commons.ModuleId;
import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.commons.TypeFactoryContext;
import org.json.JSONArray;
import org.json.JSONObject;

public class TypeFactoryContextAwareDecorator implements TypeFactory {

    private final ModuleId moduleId;
    private final TypeFactory delegate;

    public TypeFactoryContextAwareDecorator(TypeFactory delegate, ModuleId moduleId) {
        this.delegate = delegate;
        this.moduleId = moduleId;
    }

    @Override
    public boolean isPrimitive(Class<?> clazz) {
        return delegate.isPrimitive(clazz);
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONObject jsonObject, String propertyName) {
        TypeFactoryContext context = new TypeFactoryContext(moduleId);
        return create(expectedClass, jsonObject, propertyName, context);
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONArray jsonArray, int index) {
        TypeFactoryContext context = new TypeFactoryContext(moduleId);
        return create(expectedClass, jsonArray, index, context);
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONObject jsonObject, String propertyName, TypeFactoryContext context) {
        return delegate.create(expectedClass, jsonObject, propertyName, context);
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONArray jsonArray, int index, TypeFactoryContext context) {
        return delegate.create(expectedClass, jsonArray, index, context);
    }
}
