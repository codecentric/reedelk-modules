package com.reedelk.rest.component;

import com.reedelk.rest.client.*;
import com.reedelk.rest.client.strategy.ExecutionStrategy;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.component.ProcessorAsync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.NMapEvaluation;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.apache.http.nio.client.HttpAsyncClient;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("REST Client")
@Component(service = RestClient.class, scope = PROTOTYPE)
public class RestClient implements ProcessorAsync {

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private HttpClientService httpClientService;

    @Property("Method")
    @Default("GET")
    private RestMethod method;

    @Property("Client config")
    private ClientConfiguration configuration;

    @Property("Base URL")
    @Hint("https://api.example.com")
    @When(propertyName = "configuration", propertyValue = When.NULL)
    @When(propertyName = "configuration", propertyValue = "{'configRef': '" + When.BLANK + "'}")
    private String baseURL;

    @Property("Path")
    @Hint("/resource/{id}")
    private String path;

    @Property("Body")
    @ScriptInline
    @Hint("payload")
    @Default("#[payload]")
    @When(propertyName = "method", propertyValue = "DELETE")
    @When(propertyName = "method", propertyValue = "POST")
    @When(propertyName = "method", propertyValue = "PUT")
    private String body;

    @TabGroup("Headers and parameters")
    @Property("Headers")
    private Map<String, String> headers = new HashMap<>();

    @TabGroup("Headers and parameters")
    @Property("Path params")
    private Map<String, String> pathParameters = new HashMap<>();

    @TabGroup("Headers and parameters")
    @Property("Query params")
    private Map<String, String> queryParameters = new HashMap<>();

    private volatile UriComponent uriComponent;


    @Override
    public void apply(Message input, FlowContext flowContext, OnResult callback) {
        HttpAsyncClient client = client();

        ExecutionStrategy.get(method).execute(
                client, callback, flowContext,
                uriProvider(input, flowContext, callback),
                headerProvider(input, flowContext),
                bodyProvider(input));
    }

    public void setMethod(RestMethod method) {
        this.method = method;
    }

    public void setConfiguration(ClientConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public void setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }

    private HeaderProvider headerProvider(Message message, FlowContext flowContext) {
        return () -> {
            if (!headers.isEmpty()) {
                // User-defined headers: interpret and add them
                NMapEvaluation<String> evaluation =
                        scriptEngine.evaluate(message, flowContext, headers);
                return evaluation.map(0);
            } else {
                return new HashMap<>();
            }
        };
    }

    private BodyProvider bodyProvider(Message message) {
        return MessageBodyProvider.from(message, body, scriptEngine);
    }

    private UriProvider uriProvider(Message message, FlowContext flowContext, OnResult callback) {
        return () -> {
            try {
                String finalUri = baseURL + evaluateRequestUri(message, flowContext);
                return new URI(finalUri);
            } catch (URISyntaxException e) {
                callback.onError(e, flowContext);
                throw new ESBException("error");
            }
        };
    }

    private HttpAsyncClient client() {
        HttpAsyncClient client;
        if (configuration != null) {
            requireNonNull(configuration.getId(), "configuration id is mandatory");
            client = httpClientService.clientByConfig(configuration);
        } else {
            requireNonNull(baseURL, "base URL is mandatory");
            client = httpClientService.clientByBaseURL(baseURL);
        }
        return client;
    }

    private UriComponent uriComponent() {
        if (uriComponent == null) {
            synchronized (this) {
                if (uriComponent == null) {
                    uriComponent = new UriComponent(path);
                }
            }
        }
        return uriComponent;
    }

    private String evaluateRequestUri(Message message, FlowContext flowContext) {
        // Just evaluate if path params or query params are actually there, to save time!
        if (pathParameters.isEmpty() && queryParameters.isEmpty()) {
            return uriComponent().expand(pathParameters, queryParameters);
        } else if (pathParameters.isEmpty()) {
            // only query params
            NMapEvaluation<String> evaluation =
                    scriptEngine.evaluate(message, flowContext, queryParameters);
            Map<String, String> evaluatedQueryParameters = evaluation.map(0);
            return uriComponent().expand(pathParameters, evaluatedQueryParameters);
        } else if (queryParameters.isEmpty()) {
            // only path params
            NMapEvaluation<String> evaluation =
                    scriptEngine.evaluate(message, flowContext, pathParameters);
            Map<String, String> evaluatedPathParameters = evaluation.map(0);
            return uriComponent().expand(evaluatedPathParameters, queryParameters);
        } else {
            NMapEvaluation<String> evaluation =
                    scriptEngine.evaluate(message, flowContext, pathParameters, queryParameters);
            Map<String, String> evaluatedPathParameters = evaluation.map(0);
            Map<String, String> evaluatedQueryParameters = evaluation.map(1);
            return uriComponent().expand(evaluatedPathParameters, evaluatedQueryParameters);
        }
    }
}
