package com.reedelk.rest.component;

import com.reedelk.rest.client.*;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;

import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("REST Client")
@Component(service = RestClient.class, scope = PROTOTYPE)
public class RestClient implements ProcessorSync {

    private final HttpResponseMessageMapper mapper = new HttpResponseMessageMapper();

    @Reference
    private ScriptEngineService service;

    @Property("Method")
    @Default("GET")
    private RestMethod method;

    @Property("Client config")
    private ClientConfiguration configuration;

    @Property("Base URL")
    @Hint("https://api.example.com")
    @When(propertyName = "configuration", propertyValue = When.NULL)
    @When(propertyName = "configuration", propertyValue = "{'configRef': '" + When.BLANK + "'}")
    private String baseUrl;

    @Property("Path")
    @Hint("/resource/{id}")
    private String path;

    @Property("Body")
    @Hint("payload")
    @Default("payload")
    @When(propertyName = "method", propertyValue = "DELETE")
    @When(propertyName = "method", propertyValue = "POST")
    @When(propertyName = "method", propertyValue = "PUT")
    private String body;

    @TabGroup("Headers and parameters")
    @Property("Headers")
    private Map<String,String> headers;

    @TabGroup("Headers and parameters")
    @Property("Path params")
    private Map<String,String> uriParameters;

    @TabGroup("Headers and parameters")
    @Property("Query params")
    private Map<String,String> queryParameters;

    private volatile HttpClientWrapper client;

    private UriComponent uriComponent;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        HttpClientWrapper client = getClient();

        // Builds the request URI by replacing the URI parameters (if any)
        // and by adding the query parameters (if any).
        String requestUri = uriComponent.expand(uriParameters, queryParameters);

        final HttpResponseWrapper responseData = new HttpResponseWrapper();
        Mono<byte[]> responseBytes = client.execute(
                requestUri,
                MessageBodyProvider.from(message),
                ResponseHandlerProvider.from(responseData));

        // We block and wait until the complete response has been received.
        // Note that because of this line this component does not support
        // inbound streaming. It it only capable of streaming the body outbound.
        byte[] bytes = responseBytes.block();
        responseData.data(bytes);

        // Map the response
        return mapper.map(responseData);
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

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setConfiguration(ClientConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setUriParameters(Map<String, String> uriParameters) {
        this.uriParameters = uriParameters;
    }

    public void setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }

    private HttpClientWrapper getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = createClient();
                    uriComponent = new UriComponent(path);
                }
            }
        }
        return client;
    }

    private HttpClientWrapper createClient() {
        return ClientBuilder.get()
                .method(method)
                .baseUrl(baseUrl)
                .configuration(configuration)
                .onRequestConsumer(((request, connection) -> interpretAndAddHeaders(request)))
                .build();
    }

    private void interpretAndAddHeaders(HttpClientRequest request) {
        // Interpret and add headers
        if (headers != null) {
            headers.forEach(request::addHeader);
        }
    }
}
