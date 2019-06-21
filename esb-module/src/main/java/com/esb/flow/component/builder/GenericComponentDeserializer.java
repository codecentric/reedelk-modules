package com.esb.flow.component.builder;

import com.esb.api.component.Implementor;
import com.esb.api.exception.ESBException;
import com.esb.flow.ExecutionNode;
import com.esb.flow.FlowBuilderContext;
import com.esb.internal.commons.CollectionFactory;
import com.esb.internal.commons.JsonParser;
import com.esb.internal.commons.PrimitiveTypeConverter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

import static com.esb.commons.Preconditions.checkArgument;
import static com.esb.internal.commons.JsonParser.Component;
import static com.esb.internal.commons.JsonParser.Config;
import static com.esb.internal.commons.ReflectionUtils.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;

@SuppressWarnings("unchecked")
public class GenericComponentDeserializer {

    private static final List<String> EXCLUDED_PROPERTIES_FROM_WARNINGS =
            asList(JsonParser.Implementor.description(),
                    Config.id(),
                    Config.title());

    private static final Logger logger = LoggerFactory.getLogger(GenericComponentDeserializer.class);
    private static final Collection<String> EXCLUDED_PROPERTIES = Collections.singletonList(JsonParser.Implementor.name());

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
            if (EXCLUDED_PROPERTIES.contains(propertyName)) continue;

            Optional<Method> maybeSetter = getSetter(implementor, propertyName);
            if (maybeSetter.isPresent()) {
                Optional<String> maybeReference = isReference(componentDefinition, propertyName);
                Object deserializedObject = maybeReference.isPresent() ?
                        deserialize(maybeReference.get()) :
                        deserialize(componentDefinition, implementor, propertyName);

                Method setter = maybeSetter.get();
                setProperty(implementor, setter, deserializedObject);

            } else {
                if (!EXCLUDED_PROPERTIES_FROM_WARNINGS.contains(propertyName)) {
                    logger.warn("Could not find setter on implementor [{}] for property name [{}]. The property will be skipped",
                            implementor.getClass().getName(), propertyName);
                }
            }
        }
    }

    private Object deserialize(JSONObject componentDefinition, Implementor bean, String propertyName) {
        Object propertyValue = componentDefinition.get(propertyName);

        // Object
        if (propertyValue instanceof JSONObject) {
            return deserialize((JSONObject) propertyValue);

            // Collection
        } else if (propertyValue instanceof JSONArray) {
            SetterArgument setterArgument = argumentOf(bean, propertyName);
            checkArgument(CollectionFactory.isSupported(setterArgument.getClazz()), format("Could not map property %s: not a supported collection type", propertyName));
            return deserialize((JSONArray) propertyValue, setterArgument);

            // Primitive
        } else {
            SetterArgument setterArgument = argumentOf(bean, propertyName);
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
     * @return a de-serialized primitive type or an enum if the setter argument resolves to an enum type
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
            if (possibleConfigRef.has(Component.configRef())) {
                return Optional.ofNullable(Component.configRef(possibleConfigRef));
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
                .filter(referenceJsonObject -> reference.equals(Config.id(referenceJsonObject)))
                .findFirst()
                .orElseThrow(() -> new ESBException("Could not find configuration with id=[" + reference + "]"));
    }
}
