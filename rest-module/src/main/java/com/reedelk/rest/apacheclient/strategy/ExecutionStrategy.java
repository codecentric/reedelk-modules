package com.reedelk.rest.apacheclient.strategy;

import com.reedelk.rest.commons.RestMethod;

import java.util.HashMap;
import java.util.Map;

public class ExecutionStrategy {

    private static final Map<RestMethod,Strategy> METHOD_STRATEGY;
    static {
        Map<RestMethod,Strategy> tmp = new HashMap<>();
        tmp.put(RestMethod.GET, new GETStrategy());
        tmp.put(RestMethod.POST, new POSTStrategy());
        tmp.put(RestMethod.PUT, new PUTStrategy());
        tmp.put(RestMethod.DELETE, new DELETEStrategy());
        tmp.put(RestMethod.HEAD, new HEADStrategy());
        tmp.put(RestMethod.OPTIONS, new OPTIONStrategy());
        METHOD_STRATEGY = tmp;
    }

    public static Strategy get(RestMethod method) {
        return METHOD_STRATEGY.get(method);
    }
}
