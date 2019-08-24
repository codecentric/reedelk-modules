package com.reedelk.rest.commons;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapUtils {

    private MapUtils() {
    }

    public static Map<String,String> newMap() {
        return new LinkedHashMap<>();
    }

    public static Map<String,String> newMap(String key1, String value1) {
        Map<String,String> map = new LinkedHashMap<>();
        map.put(key1, value1);
        return map;
    }

    public static Map<String,String> newMap(String key1, String value1, String key2, String value2) {
        Map<String,String> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    public static Map<String,String> newMap(String key1, String value1, String key2, String value2, String key3, String value3) {
        Map<String,String> map = new LinkedHashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }
}
