package com.reedelk.rest.component;

import com.reedelk.rest.client.HttpClient;
import com.reedelk.rest.client.HttpClientService;
import com.reedelk.rest.client.body.BodyEvaluator;
import com.reedelk.rest.client.body.BodyProvider;
import com.reedelk.rest.client.header.HeaderProvider;
import com.reedelk.rest.client.header.HeadersEvaluator;
import com.reedelk.rest.client.strategy.ExecutionStrategyBuilder;
import com.reedelk.rest.client.strategy.Strategy;
import com.reedelk.rest.client.uri.URIEvaluator;
import com.reedelk.rest.client.uri.URIProvider;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.StreamingMode;
import com.reedelk.rest.configuration.client.AdvancedConfiguration;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.component.ProcessorAsync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicmap.DynamicStringMap;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
    @Hint("payload")
    @Default("#[payload]")
    @When(propertyName = "method", propertyValue = "DELETE")
    @When(propertyName = "method", propertyValue = "POST")
    @When(propertyName = "method", propertyValue = "PUT")
    private DynamicByteArray body;

    @Property("Streaming")
    @Default("AUTO")
    @When(propertyName = "method", propertyValue = "DELETE")
    @When(propertyName = "method", propertyValue = "POST")
    @When(propertyName = "method", propertyValue = "PUT")
    private StreamingMode streaming = StreamingMode.AUTO;

    @TabGroup("Headers and parameters")
    @Property("Headers")
    private DynamicStringMap headers = DynamicStringMap.empty();

    @TabGroup("Headers and parameters")
    @Property("Path params")
    private DynamicStringMap pathParameters = DynamicStringMap.empty();

    @TabGroup("Headers and parameters")
    @Property("Query params")
    private DynamicStringMap queryParameters = DynamicStringMap.empty();

    @Property("Advanced configuration")
    private AdvancedConfiguration advancedConfiguration;

    private volatile Strategy execution;
    private volatile URIEvaluator uriEvaluator;
    private volatile BodyEvaluator bodyEvaluator;
    private volatile HeadersEvaluator headersEvaluator;


    @Override
    public void apply(Message message, FlowContext flowContext, OnResult callback) {
        HttpClient client = client();

        BodyProvider bodyProvider = bodyEvaluator().provider();

        URIProvider uriProvider = uriEvaluator().provider(message, flowContext);

        HeaderProvider headerProvider = headersEvaluator().provider(message, flowContext);

        execution().execute(client, callback, message, flowContext,
                uriProvider, headerProvider, bodyProvider);
    }

    @Override
    public void dispose() {
        this.scriptEngine = null;
        this.httpClientService = null;
        this.uriEvaluator = null;
        this.bodyEvaluator = null;
        this.headersEvaluator = null;
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

    public void setBody(DynamicByteArray body) {
        this.body = body;
    }

    public void setStreaming(StreamingMode streaming) {
        this.streaming = streaming;
    }

    public void setHeaders(DynamicStringMap headers) {
        this.headers = headers;
    }

    public void setPathParameters(DynamicStringMap pathParameters) {
        this.pathParameters = pathParameters;
    }

    public void setQueryParameters(DynamicStringMap queryParameters) {
        this.queryParameters = queryParameters;
    }

    public void setAdvancedConfiguration(AdvancedConfiguration advancedConfiguration) {
        this.advancedConfiguration = advancedConfiguration;
    }

    private HttpClient client() {
        if (configuration != null) {
            requireNonNull(configuration.getId(), "configuration id is mandatory");
            return httpClientService.clientByConfig(configuration);
        } else {
            requireNonNull(baseURL, "base URL is mandatory");
            return httpClientService.clientByBaseURL(baseURL);
        }
    }

    private Strategy execution() {
        if (execution == null) {
            synchronized (this) {
                if (execution == null) {
                    execution = ExecutionStrategyBuilder.builder()
                            .advancedConfig(advancedConfiguration)
                            .streaming(streaming)
                            .method(method)
                            .build();
                }
            }
        }
        return execution;
    }

    private URIEvaluator uriEvaluator() {
        if (uriEvaluator == null) {
            synchronized (this) {
                if (uriEvaluator == null) {
                    uriEvaluator = URIEvaluator.builder()
                            .queryParameters(queryParameters)
                            .pathParameters(pathParameters)
                            .configuration(configuration)
                            .scriptEngine(scriptEngine)
                            .baseURL(baseURL)
                            .path(path)
                            .build();
                }
            }
        }
        return uriEvaluator;
    }

    private BodyEvaluator bodyEvaluator() {
        if (bodyEvaluator == null) {
            synchronized (this) {
                if (bodyEvaluator == null) {
                    bodyEvaluator = BodyEvaluator.builder()
                            .scriptEngine(scriptEngine)
                            .method(method)
                            .body(body)
                            .build();
                }
            }
        }
        return bodyEvaluator;
    }

    private HeadersEvaluator headersEvaluator() {
        if (headersEvaluator == null) {
            synchronized (this) {
                if (headersEvaluator == null) {
                    headersEvaluator = HeadersEvaluator.builder()
                            .scriptEngine(scriptEngine)
                            .headers(headers)
                            .body(body)
                            .build();
                }
            }
        }
        return headersEvaluator;
    }
}
