package com.esb.converter;

import com.esb.api.component.Implementor;
import com.esb.api.exception.ESBException;
import com.esb.commons.JsonParser;
import com.esb.commons.ReflectionUtils;
import com.esb.flow.ExecutionNode;
import com.esb.flow.FlowBuilderContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static com.esb.commons.Preconditions.checkArgument;
import static com.esb.commons.ReflectionUtils.SetterArgument;
import static com.esb.commons.ReflectionUtils.argumentOf;
import static java.lang.String.format;

@SuppressWarnings("unchecked")
public class JSONDeserializer {

    private final ExecutionNode executionNode;
    private final FlowBuilderContext context;

    public JSONDeserializer(final ExecutionNode executionNode, final FlowBuilderContext context) {
        this.executionNode = executionNode;
        this.context = context;
    }

    public void deserialize(JSONObject componentDefinition, Implementor implementor) {
        Iterator<String> iterator = componentDefinition.keys();

        while (iterator.hasNext()) {
            String propertyName = iterator.next();
            if (JsonParser.Implementor.name().equals(propertyName)) continue;

            Optional<String> optionalReference = isReference(componentDefinition, propertyName);
            Object deserialized = optionalReference.isPresent() ?
                    deserialize(optionalReference.get()) :
                    deserialize(componentDefinition, implementor, propertyName);
            ReflectionUtils.setPropertyIfExists(implementor, propertyName, deserialized);
        }
    }

    private Object deserialize(JSONObject componentDefinition, Implementor bean, String propertyName) {
        Object propertyValue = componentDefinition.get(propertyName);

        // Object
        if (propertyValue instanceof JSONObject) {
            return deserialize((JSONObject) propertyValue);

            // Collection
        } else if (propertyValue instanceof JSONArray) {
            Optional<SetterArgument> optionalSetterArg = argumentOf(bean, propertyName);
            checkArgument(optionalSetterArg.isPresent(), format("Could not find setter for property %s", propertyName));
            checkArgument(optionalSetterArg.get().isSupportedCollection(), format("Could not map property %s. Not a supported collection", propertyName));
            SetterArgument setterArgument = optionalSetterArg.get();
            return deserialize((JSONArray) propertyValue, setterArgument);

            // Primitive
        } else {
            Optional<SetterArgument> optionalSetterArg = argumentOf(bean, propertyName);
            checkArgument(optionalSetterArg.isPresent(), format("Could not find setter for property %s", propertyName));
            SetterArgument setterArgument = optionalSetterArg.get();
            return deserialize(componentDefinition, propertyName, setterArgument);
        }
    }


    /**
     * Deserialize a JSON object. If exists an Implementor class defined in OSGi, then it is used.
     * Otherwise the JSON object is mapped as a Java Map.
     *
     * @param object the JSON object to be deserialized
     * @return a deserialized Java Instance of the JSON object or a Java Map representing the JSON object
     */
    private Object deserialize(JSONObject object) {
        if (object.has(JsonParser.Implementor.name())) {
            Implementor deserialized = instantiateImplementor(object);
            deserialize(object, deserialized);
            return deserialized;
        } else {
            return object.toMap();
        }
    }

    /**
     * Deserialize a JSON array. The deserialized collection contains element converted
     * to the type of the corresponding given setter argument.
     *
     * @param array    the JSON array to be deserialized
     * @param argument the bean setter argument
     * @return a deserialized Java collection (Collection,List,Set) representing the JSON array
     */
    private Collection deserialize(JSONArray array, SetterArgument argument) {
        Class<Collection> clazz = (Class<Collection>) argument.getClazz();
        Collection collection = CollectionFactory.from(clazz);
        Class<?> genericType = argument.getGenericType();
        for (int index = 0; index < array.length(); index++) {
            Object converted = PrimitiveTypeConverter.convert(genericType, array, index);
            collection.add(converted);
        }
        return collection;
    }

    /**
     * Deserialize a JSON primitive type. The deserialized primitive type converted
     * to the given setter argument type.
     *
     * @param componentDefinition the JSON object holding the primitive type
     * @param propertyName        the name of the JSON object's property for which we want the primitive type deserialized
     * @param setterArgument      the bean setter argument
     * @return a deserialized primitive type or an enum if the setter argument resolves to an enum type
     */
    private <E extends Enum<E>> Object deserialize(JSONObject componentDefinition, String propertyName, SetterArgument setterArgument) {
        if (setterArgument.isEnum()) {
            Class<E> enumClazz = setterArgument.getClazz();
            return componentDefinition.getEnum(enumClazz, propertyName);
        } else {
            Class<?> clazz = setterArgument.getClazz();
            return PrimitiveTypeConverter.convert(clazz, componentDefinition, propertyName);
        }
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
            if (possibleConfigRef.has(JsonParser.Component.configRef())) {
                return Optional.ofNullable(JsonParser.Component.configRef(possibleConfigRef));
            }
        }
        return Optional.empty();
    }

    private Implementor instantiateImplementor(JSONObject jsonObject) {
        String implementorFullyQualifiedName = JsonParser.Implementor.name(jsonObject);
        return context.instantiateImplementor(executionNode, implementorFullyQualifiedName);
    }

    private JSONObject findReferenceDefinition(String reference) {
        return context.getDeserializedModule()
                .getConfigurations()
                .stream()
                .filter(referenceJsonObject -> reference.equals(JsonParser.Config.id(referenceJsonObject)))
                .findFirst()
                .orElseThrow(() ->
                        new ESBException("Could not find configuration with id=[" + reference + "]"));
    }
}
