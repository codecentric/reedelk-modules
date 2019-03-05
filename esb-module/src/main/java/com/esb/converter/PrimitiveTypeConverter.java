package com.esb.converter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static com.esb.commons.Preconditions.checkArgument;
import static java.lang.String.format;

class PrimitiveTypeConverter {

    private static final Map<Class, Converter> OBJECT_CONVERTER;

    static {
        OBJECT_CONVERTER = new HashMap<>();
        OBJECT_CONVERTER.put(String.class, new AsString());
        OBJECT_CONVERTER.put(Long.class, new AsLong());
        OBJECT_CONVERTER.put(long.class, new AsLong());
        OBJECT_CONVERTER.put(Integer.class, new AsInt());
        OBJECT_CONVERTER.put(int.class, new AsInt());
        OBJECT_CONVERTER.put(Double.class, new AsDouble());
        OBJECT_CONVERTER.put(double.class, new AsDouble());
        OBJECT_CONVERTER.put(Float.class, new AsFloat());
        OBJECT_CONVERTER.put(float.class, new AsFloat());
        OBJECT_CONVERTER.put(Boolean.class, new AsBoolean());
        OBJECT_CONVERTER.put(boolean.class, new AsBoolean());
        OBJECT_CONVERTER.put(Number.class, new AsNumber());
        OBJECT_CONVERTER.put(BigDecimal.class, new AsBigDecimal());
        OBJECT_CONVERTER.put(BigInteger.class, new AsBigInteger());
    }

    static Object convert(Class<?> expectedClass, JSONObject jsonObject, String propertyName) {
        checkArgument(expectedClass != null, "expectedClass");
        checkArgument(propertyName != null, "propertyName");
        checkArgument(jsonObject != null, "jsonObject");

        if (OBJECT_CONVERTER.containsKey(expectedClass)) {
            return OBJECT_CONVERTER.get(expectedClass).convert(jsonObject, propertyName);
        }
        throw new IllegalStateException(format("Could not convert property with name '%s' to Class '%s'", propertyName, expectedClass.getName()));
    }

    static Object convert(Class<?> expectedClass, JSONArray jsonArray, int index) {
        checkArgument(expectedClass != null, "expectedClass");
        checkArgument(jsonArray != null, "jsonArray");

        if (OBJECT_CONVERTER.containsKey(expectedClass)) {
            return OBJECT_CONVERTER.get(expectedClass).convert(jsonArray, index);
        }
        throw new IllegalStateException(format("Could not convert property with name '%d' to Class '%s'", index, expectedClass.getName()));
    }


    interface Converter {
        Object convert(JSONObject object, String key);

        Object convert(JSONArray array, int index);
    }

    static class AsString implements Converter {
        @Override
        public Object convert(JSONObject object, String key) {
            return object.getString(key);
        }

        @Override
        public Object convert(JSONArray array, int index) {
            return array.getString(index);
        }
    }

    static class AsLong implements Converter {
        @Override
        public Object convert(JSONObject object, String key) {
            return object.getLong(key);
        }

        @Override
        public Object convert(JSONArray array, int index) {
            return array.getLong(index);
        }
    }

    static class AsInt implements Converter {
        @Override
        public Object convert(JSONObject object, String key) {
            return object.getInt(key);
        }

        @Override
        public Object convert(JSONArray array, int index) {
            return array.getInt(index);
        }
    }

    static class AsDouble implements Converter {
        @Override
        public Object convert(JSONObject object, String key) {
            return object.getDouble(key);
        }

        @Override
        public Object convert(JSONArray array, int index) {
            return array.getDouble(index);
        }
    }

    static class AsFloat implements Converter {
        @Override
        public Object convert(JSONObject object, String key) {
            return object.getFloat(key);
        }

        @Override
        public Object convert(JSONArray array, int index) {
            return array.getFloat(index);
        }
    }

    static class AsBoolean implements Converter {
        @Override
        public Object convert(JSONObject object, String key) {
            return object.getBoolean(key);
        }

        @Override
        public Object convert(JSONArray array, int index) {
            return array.getBoolean(index);
        }
    }

    static class AsNumber implements Converter {
        @Override
        public Object convert(JSONObject object, String key) {
            return object.getNumber(key);
        }

        @Override
        public Object convert(JSONArray array, int index) {
            return array.getNumber(index);
        }
    }

    static class AsBigDecimal implements Converter {
        @Override
        public Object convert(JSONObject object, String key) {
            return object.getBigDecimal(key);
        }

        @Override
        public Object convert(JSONArray array, int index) {
            return array.getBigDecimal(index);
        }
    }

    static class AsBigInteger implements Converter {
        @Override
        public Object convert(JSONObject object, String key) {
            return object.getBigInteger(key);
        }

        @Override
        public Object convert(JSONArray array, int index) {
            return array.getBigInteger(index);
        }
    }

}
