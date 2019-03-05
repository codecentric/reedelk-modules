package com.esb.commons;

import com.esb.converter.CollectionFactory;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectionUtils {

    private static final Pattern MATCH_GENERIC_TYPE = Pattern.compile(".*<(.*)>.*");

    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

    private ReflectionUtils() {
    }

    public static void setPropertyIfExists(Object source, String propertyName, Object value) {
        Method matchingMethod = MethodUtils.getMatchingMethod(source.getClass(), setterName(propertyName), value.getClass());
        if (matchingMethod != null) {
            try {
                matchingMethod.invoke(source, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.warn(e.getLocalizedMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Optional<SetterArgument> argumentOf(Object source, String propertyName) {
        Optional<Method> optionalMethod = getSetter(source, setterName(propertyName));
        if (!optionalMethod.isPresent()) {
            logger.warn("Could not find setter for property with name {}", propertyName);
            return Optional.empty();
        }

        Method method = optionalMethod.get();
        if (method.getParameterTypes().length != 1) {
            throw new IllegalStateException("Setter for property must have one argument!");
        }

        Class<?> parameterType = method.getParameterTypes()[0];

        Optional<String> optionalGenericType = getGenericType(method);
        if (optionalGenericType.isPresent()) {
            try {
                return Optional.of(new SetterArgument(parameterType, Class.forName(optionalGenericType.get())));
            } catch (ClassNotFoundException e) {
                logger.warn("Could not find class for generic type {}", optionalGenericType.get());
            }
        }

        return Optional.of(new SetterArgument(parameterType));
    }

    private static Optional<Method> getSetter(Object object, String methodName) {
        Method[] declaredMethods = object.getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            String name = method.getName();
            if (name.equals(methodName)) return Optional.of(method);
        }
        return Optional.empty();
    }

    private static Optional<String> getGenericType(Method method) {
        Type genericParameterType = method.getGenericParameterTypes()[0];
        String typeName = genericParameterType.getTypeName();
        Matcher matcher = MATCH_GENERIC_TYPE.matcher(typeName);

        if (matcher.matches() && matcher.groupCount() > 0) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    private static String setterName(String propertyName) {
        return "set" + capitalize(propertyName);
    }

    private static String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static class SetterArgument<C, G> {

        private final Class<C> clazz;
        private final Class<G> genericType; // TODO: What if the generic type is defined in the bundle

        SetterArgument(Class<C> clazz) {
            this.clazz = clazz;
            this.genericType = null;
        }

        SetterArgument(Class<C> clazz, Class<G> genericType) {
            this.clazz = clazz;
            this.genericType = genericType;
        }

        public Class<C> getClazz() {
            return clazz;
        }

        public boolean isSupportedCollection() {
            return CollectionFactory.SUPPORTED_COLLECTIONS.contains(this.clazz);
        }

        public Class<G> getGenericType() {
            return genericType;
        }

        public boolean isEnum() {
            return clazz.isEnum();
        }
    }
}
