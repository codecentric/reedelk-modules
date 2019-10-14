package com.reedelk.esb.flow.deserializer;

import com.reedelk.esb.flow.FlowBuilderContext;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.commons.CollectionFactory;
import com.reedelk.runtime.commons.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static com.reedelk.esb.commons.Preconditions.checkArgument;
import static com.reedelk.runtime.commons.JsonParser.Component;
import static com.reedelk.runtime.commons.JsonParser.Config;
import static com.reedelk.runtime.commons.ReflectionUtils.*;
import static java.lang.String.format;

public class ComponentDefinitionDeserializer {

    private final ExecutionNode executionNode;
    private final FlowBuilderContext context;

    ComponentDefinitionDeserializer(final ExecutionNode executionNode, final FlowBuilderContext context) {
        this.executionNode = executionNode;
        this.context = context;
    }

    public void deserialize(JSONObject componentDefinition, Implementor implementor) {
        Iterator<String> iterator = componentDefinition.keys();
        while (iterator.hasNext()) {
            String propertyName = iterator.next();
            getSetter(implementor, propertyName).ifPresent(setter -> {
                Optional<String> maybeReference = isReference(componentDefinition, propertyName);
                Object deSerializedObject = maybeReference.isPresent() ?
                        deserialize(maybeReference.get()) :
                        deserialize(componentDefinition, implementor, propertyName);
                setProperty(implementor, setter, deSerializedObject);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private Object deserialize(JSONObject componentDefinition, Implementor bean, String propertyName) {
        Object propertyValue = componentDefinition.get(propertyName);
        SetterArgument setterArgument = argumentOf(bean, propertyName);

        // Dynamic Map or declared Implementor object
        if (propertyValue instanceof JSONObject) {
            return deserializeObject(componentDefinition, propertyName, setterArgument);

            // Collection
        } else if (propertyValue instanceof JSONArray) {
            checkArgument(CollectionFactory.isSupported(setterArgument.getClazz()),
                    format("Could not map property %s: not a supported collection type", propertyName));
            return deserializeArray(componentDefinition, propertyName, setterArgument);

            // Enum
        } else if (setterArgument.isEnum()){
            Class enumClazz = setterArgument.getClazz();
            return context.convert(enumClazz, componentDefinition, propertyName);

            // Primitive or Dynamic Value
        } else {
            Class<?> clazz = setterArgument.getClazz();
            return context.convert(clazz, componentDefinition, propertyName);
        }
    }

    private Object deserializeObject(JSONObject componentDefinition, String propertyName, SetterArgument setterArgument) {
        if (setterArgument.isMap()) {
            // The setter argument for this property is a map, so we just return
            // a de-serialized java map object.
            JSONObject jsonObject = componentDefinition.getJSONObject(propertyName);
            return jsonObject.toMap();
        } else if (setterArgument.isDynamicMap()){
            // The setter argument for this property is any type of Dynamic map,
            // we must wrap the de-serialized java map object with a type specific
            // dynamic map which adds a UUID identifying the dynamic map function to
            // be used by the Script engine as a reference for the pre-compiled script
            // to be used at runtime evaluation.
            Class<?> clazz = setterArgument.getClazz();
            return context.convert(clazz, componentDefinition, propertyName);
        } else {
            // It is a complex type implementing implementor interface.
            // We expect that this JSONObject satisfies the properties
            // of the property setter argument's object type.
            String fullyQualifiedName = setterArgument.getFullyQualifiedName();
            Implementor deSerialized = instantiateImplementor(fullyQualifiedName);
            JSONObject jsonObject = componentDefinition.getJSONObject(propertyName);
            deserialize(jsonObject, deSerialized);
            return deSerialized;
        }
    }

    @SuppressWarnings("unchecked")
    private Collection deserializeArray(JSONObject componentDefinition, String propertyName, SetterArgument argument) {
        JSONArray array = componentDefinition.getJSONArray(propertyName);
        Class clazz = argument.getClazz();
        Collection collection = CollectionFactory.from(clazz);
        Class<?> genericType = argument.getGenericType();
        for (int index = 0; index < array.length(); index++) {
            Object converted = context.convert(genericType, array, index);
            collection.add(converted);
        }
        return collection;
    }

    private Object deserialize(String referenceId) {
        JSONObject jsonConfig = findReferenceDefinition(referenceId);
        Implementor bean = instantiateImplementor(jsonConfig);
        deserialize(jsonConfig, bean);
        return bean;
    }

    private Optional<String> isReference(JSONObject componentDefinition, String propertyName) {
        Object propertyValue = componentDefinition.get(propertyName);
        if (propertyValue instanceof JSONObject) {
            JSONObject possibleConfigRef = (JSONObject) propertyValue;
            if (possibleConfigRef.has(Component.configRef())) {
                return Optional.ofNullable(Component.configRef(possibleConfigRef));
            }
        }
        return Optional.empty();
    }

    private Implementor instantiateImplementor(JSONObject jsonObject) {
        String implementorFullyQualifiedName = JsonParser.Implementor.name(jsonObject);
        return instantiateImplementor(implementorFullyQualifiedName);
    }

    private Implementor instantiateImplementor(String implementorFullyQualifiedName) {
        return context.instantiateImplementor(executionNode, implementorFullyQualifiedName);
    }

    private JSONObject findReferenceDefinition(String reference) {
        return context.getDeSerializedModule()
                .getConfigurations()
                .stream()
                .filter(referenceJsonObject -> reference.equals(Config.id(referenceJsonObject)))
                .findFirst()
                .orElseThrow(() -> new ESBException("Could not find configuration with id=[" + reference + "]"));
    }
}
