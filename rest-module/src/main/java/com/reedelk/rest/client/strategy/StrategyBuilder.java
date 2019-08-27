package com.reedelk.rest.client.strategy;

import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.runtime.api.exception.ESBException;
import reactor.netty.http.client.HttpClient;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class StrategyBuilder {

    private static final Map<RestMethod,Class<? extends ExecutionStrategy>> EXECUTION_STRATEGY_MAP;
    static {
        Map<RestMethod,Class<? extends ExecutionStrategy>> tmp = new HashMap<>();
        tmp.put(RestMethod.GET, GetStrategy.class);
        tmp.put(RestMethod.PUT, PutStrategy.class);
        tmp.put(RestMethod.POST, PostStrategy.class);
        tmp.put(RestMethod.HEAD, HeadStrategy.class);
        tmp.put(RestMethod.DELETE, DeleteStrategy.class);
        tmp.put(RestMethod.OPTIONS, OptionsStrategy.class);
        EXECUTION_STRATEGY_MAP = tmp;
    }

    private RestMethod method;

    private String baseURL;
    private HttpClient client;
    private boolean followRedirects;

    public static StrategyBuilder get() {
        return new StrategyBuilder();
    }

    public StrategyBuilder method(RestMethod method) {
        this.method = method;
        return this;
    }

    public StrategyBuilder client(HttpClient client) {
        this.client = client;
        return this;
    }

    public StrategyBuilder baseURL(String baseURL) {
        this.baseURL = baseURL;
        return this;
    }

    public StrategyBuilder followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public ExecutionStrategy build() {
        Class<? extends ExecutionStrategy> executionStrategyClazz = EXECUTION_STRATEGY_MAP.get(method);
        return instantiateBuilder(executionStrategyClazz);
    }

    private ExecutionStrategy instantiateBuilder(Class<? extends ExecutionStrategy> clazz) {
        try {
            return clazz
                    .getDeclaredConstructor(HttpClient.class, String.class, boolean.class)
                    .newInstance(client, baseURL, followRedirects);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ESBException(e);
        }
    }
}
