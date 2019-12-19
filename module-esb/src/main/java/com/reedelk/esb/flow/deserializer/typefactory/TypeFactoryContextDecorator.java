package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.commons.TypeFactoryContext;
import org.json.JSONArray;
import org.json.JSONObject;

public class TypeFactoryContextDecorator implements TypeFactory {

    private final TypeFactory delegate;
    private final TypeFactoryContext context;

    public TypeFactoryContextDecorator(TypeFactory delegate, long moduleId) {
        this.delegate = delegate;
        this.context = new TypeFactoryContext(moduleId);
    }

    @Override
    public boolean isPrimitive(Class<?> clazz) {
        return delegate.isPrimitive(clazz);
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONObject jsonObject, String propertyName) {
        return create(expectedClass, jsonObject, propertyName, context);
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONArray jsonArray, int index) {
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
