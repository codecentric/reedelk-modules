package com.reedelk.rest.client.strategy;

import com.reedelk.rest.commons.RestMethod;

import java.util.HashMap;
import java.util.Map;

public class ExecutionStrategy {

    private static final Map<RestMethod,Strategy> METHOD_STRATEGY;
    static {
        Map<RestMethod,Strategy> tmp = new HashMap<>();
        tmp.put(RestMethod.GET, new GET());
        tmp.put(RestMethod.PUT, new PUT());
        tmp.put(RestMethod.POST, new POST());
        tmp.put(RestMethod.HEAD, new HEAD());
        tmp.put(RestMethod.DELETE, new DELETE());
        tmp.put(RestMethod.OPTIONS, new OPTIONS());
        METHOD_STRATEGY = tmp;
    }

    public static Strategy get(RestMethod method) {
        return METHOD_STRATEGY.get(method);
    }
}
