package com.reedelk.esb.flow.component.builder;

import com.reedelk.esb.flow.FlowBuilderContext;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.Implementor;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.commons.CollectionFactory;
import com.reedelk.runtime.commons.JsonParser;
import com.reedelk.runtime.commons.JsonTypeConverter;
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

@SuppressWarnings("unchecked")
public class GenericComponentDeserializer {

    private final ExecutionNode executionNode;
    private final FlowBuilderContext context;

    public GenericComponentDeserializer(final ExecutionNode executionNode, final FlowBuilderContext context) {
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

    private Object deserialize(JSONObject componentDefinition, Implementor bean, String propertyName) {
        Object propertyValue = componentDefinition.get(propertyName);
        SetterArgument setterArgument = argumentOf(bean, propertyName);

        // Dynamic Map or declared Implementor object
        if (propertyValue instanceof JSONObject) {
            return deserialize((JSONObject) propertyValue, propertyName, setterArgument);

            // Collection
        } else if (propertyValue instanceof JSONArray) {
            checkArgument(CollectionFactory.isSupported(setterArgument.getClazz()), format("Could not map property %s: not a supported collection type", propertyName));
            return deserialize((JSONArray) propertyValue, setterArgument);

            // Enum
        } else if (setterArgument.isEnum()){
            Class enumClazz = setterArgument.getClazz();
            return componentDefinition.getEnum(enumClazz, propertyName);

            // Primitive or Dynamic Value
        } else {
            Class<?> clazz = setterArgument.getClazz();
            return JsonTypeConverter.convert(clazz, componentDefinition, propertyName);
        }
    }


    /**
     * Deserialize a JSON object. If exists an Implementor class defined in OSGi, then it is used.
     * Otherwise the JSON object is mapped as a Java Map.
     *
     * @param object         the JSON object to be de-serialized
     * @param setterArgument the setter argument of the field this object represents.
     * @return a de-serialized Java Instance of the JSON object or a Java Map representing the JSON object
     */
    private Object deserialize(JSONObject object, String propertyName, SetterArgument setterArgument) {
        if (setterArgument.isMap()) {
            // The setter argument for this property is a map, so we just return
            // a de-serialized java map object.
            return object.toMap();
        } else if (setterArgument.isDynamicMap()){
            // The setter argument for this property is any type of Dynamic map,
            // we must wrap the de-serialized java map object with a type specific
            // dynamic map which adds a UUID identifying the dynamic map function to
            // be used by the Script engine as a reference for the pre-compiled script
            // to be used at runtime evaluation.
            Class<?> clazz = setterArgument.getClazz();
            return JsonTypeConverter.convert(clazz, object, propertyName);
        } else {
            // It is a complex type implementing implementor interface.
            // We expect that this JSONObject satisfies the properties
            // of the property setter argument's object type.
            String fullyQualifiedName = setterArgument.getFullyQualifiedName();
            Implementor deSerialized = instantiateImplementor(fullyQualifiedName);
            deserialize(object, deSerialized);
            return deSerialized;
        }
    }

    /**
     * Deserialize a JSON array. The de-serialized collection contains element converted
     * to the type of the corresponding given setter argument.
     *
     * @param array    the JSON array to be de-serialized
     * @param argument the bean setter argument
     * @return a de-serialized Java collection (Collection,List,Set) representing the JSON array
     */
    private Collection deserialize(JSONArray array, SetterArgument argument) {
        Class<Collection> clazz = (Class<Collection>) argument.getClazz();
        Collection collection = CollectionFactory.from(clazz);
        Class<?> genericType = argument.getGenericType();
        for (int index = 0; index < array.length(); index++) {
            Object converted = JsonTypeConverter.convert(genericType, array, index);
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
        return context.getDeserializedModule()
                .getConfigurations()
                .stream()
                .filter(referenceJsonObject -> reference.equals(Config.id(referenceJsonObject)))
                .findFirst()
                .orElseThrow(() -> new ESBException("Could not find configuration with id=[" + reference + "]"));
    }
}
