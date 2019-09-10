package com.reedelk.rest.component;

import com.reedelk.rest.configuration.ListenerConfiguration;
import com.reedelk.rest.configuration.ListenerErrorResponse;
import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.rest.server.HttpRequestHandler;
import com.reedelk.rest.server.Server;
import com.reedelk.rest.server.ServerProvider;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.AbstractInbound;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("REST Listener")
@Component(service = RestListener.class, scope = PROTOTYPE)
public class RestListener extends AbstractInbound {

    private static final Logger logger = LoggerFactory.getLogger(RestListener.class);

    @Reference
    private ServerProvider provider;

    @Reference
    private ScriptEngineService scriptEngine;

    @Property("Configuration")
    private ListenerConfiguration configuration;

    @Property("Path")
    @Default("/")
    @Hint("/resource")
    private String path;

    @Property("Method")
    @Default("GET")
    private RestMethod method;

    @ScriptInline
    @Default("#[payload]")
    @Hint("content body text")
    @Property("Response body")
    private String body;

    @ScriptInline
    @Default("200")
    @Hint("201")
    @Property("Response status")
    private String status;

    @TabGroup("Response headers")
    @Property("Response headers")
    private Map<String, String> headers = Collections.emptyMap();

    @Property("Use error response")
    private Boolean useErrorResponse;

    @Property("Error response")
    @When(propertyName = "useErrorResponse", propertyValue = "true")
    private ListenerErrorResponse errorResponse;


    @Override
    public void onStart() {
        requireNonNull(configuration, "configuration");
        requireNonNull(method, "method");
        requireNonNull(path, "path");

        Server server = provider.get(configuration);

        HttpRequestHandler httpRequestHandler =
                HttpRequestHandler.builder()
                        .responseBody(body)
                        .responseHeaders(headers)
                        .responseStatus(status)
                        .scriptEngine(scriptEngine)
                        .errorResponse(errorResponse)
                        .inboundEventListener(RestListener.this)
                        .build();
        server.addRoute(method, path, httpRequestHandler);
    }

    @Override
    public void onShutdown() {
        Server server = provider.get(configuration);
        server.removeRoute(method, path);
        try {
            provider.release(server);
        } catch (Exception e) {
            logger.error("Shutdown RESTListener", e);
        }
    }

    public void setConfiguration(ListenerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMethod(RestMethod method) {
        this.method = method;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setUseErrorResponse(Boolean useErrorResponse) {
        this.useErrorResponse = useErrorResponse;
    }

    public void setErrorResponse(ListenerErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }
}
