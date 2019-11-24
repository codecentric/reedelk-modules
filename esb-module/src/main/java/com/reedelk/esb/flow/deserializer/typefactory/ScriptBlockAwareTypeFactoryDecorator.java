package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.commons.TypeFactoryContext;
import org.json.JSONArray;
import org.json.JSONObject;

public class ScriptBlockAwareTypeFactoryDecorator implements TypeFactory {

    private final long moduleId;
    private final String flowId;
    private final String flowTitle;
    private final TypeFactory delegate;

    public ScriptBlockAwareTypeFactoryDecorator(TypeFactory delegate, long moduleId, String flowId, String flowTitle) {
        this.delegate = delegate;
        this.moduleId = moduleId;
        this.flowId = flowId;
        this.flowTitle = flowTitle;
    }

    @Override
    public boolean isPrimitive(Class<?> clazz) {
        return delegate.isPrimitive(clazz);
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONObject jsonObject, String propertyName) {
        TypeFactoryContext context = new TypeFactoryContext(moduleId, flowId, flowTitle);
        return create(expectedClass, jsonObject, propertyName, context);
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONArray jsonArray, int index) {
        TypeFactoryContext context = new TypeFactoryContext(moduleId, flowId, flowTitle);
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
