package com.reedelk.rest.component;

import com.reedelk.rest.client.ClientBuilder;
import com.reedelk.rest.client.ExtractTypeFromHeaders;
import com.reedelk.rest.client.HttpClientWrapper;
import com.reedelk.rest.client.UriComponent;
import com.reedelk.rest.configuration.RestClientConfiguration;
import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.ByteArrayType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.service.ScriptEngineService;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;

import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("REST Client")
@Component(service = RestClient.class, scope = PROTOTYPE)
public class RestClient implements ProcessorSync {

    @Reference
    private ScriptEngineService service;

    @Property("Method")
    private RestMethod method;

    @Property("Use client config")
    @Default("false")
    private boolean useConfiguration;

    @Property("Base URL")
    @Hint("https://api.example.com")
    @When(propertyName = "useConfiguration", propertyValue = "false")
    private String baseUrl;

    @Property("Client config")
    @When(propertyName = "useConfiguration", propertyValue = "true")
    private RestClientConfiguration configuration;

    @Property("Path")
    @Hint("/resource/{id}")
    private String path;

    @Property("Request body")
    @Hint("payload")
    @Default("payload")
    private String body;

    @TabGroup("Headers and parameters")
    @Property("Headers")
    private Map<String, String> headers;

    @TabGroup("Headers and parameters")
    @Property("Path params")
    private Map<String, String> uriParameters;

    @TabGroup("Headers and parameters")
    @Property("Query params")
    private Map<String, String> queryParameters;

    private volatile HttpClientWrapper client;

    private UriComponent uriComponent;

    @Override
    public Message apply(Message input) {
        HttpClientWrapper client = getClient();

        String uri = buildUri();

        RequestData data = new RequestData();
        Mono<byte[]> dataHolder = client.execute(uri, (response, byteBufMono) -> {

            data.headers = response.responseHeaders();
            data.status = response.status();
            return byteBufMono.asByteArray();
            // Body provider only if it is POST
        }, () -> Flux.just(Unpooled.wrappedBuffer(input.getTypedContent().asByteArray())));

        byte[] bytes = dataHolder.block();

        Type type = ExtractTypeFromHeaders.from(data.headers);
        TypedContent content = new ByteArrayType(bytes, type);
        input.setTypedContent(content);
        return input;
    }

    private class RequestData {
        private HttpHeaders headers;
        private HttpResponseStatus status;
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

    public void setUseConfiguration(boolean useConfiguration) {
        this.useConfiguration = useConfiguration;
    }

    public void setConfiguration(RestClientConfiguration configuration) {
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

    /**
     * It replaces from the original path all the path parameters,
     * and it appends query parameters at the end.
     */
    private String buildUri() {
        return uriComponent.expand(uriParameters, queryParameters);
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
                .useConfiguration(useConfiguration)
                .onRequestConsumer(((request, connection) -> interpretAndAddHeaders(request)))
                .build();
    }

    private void interpretAndAddHeaders(HttpClientRequest request) {
        // Interpret headers
        if (headers != null) {
            headers.forEach(request::addHeader);
        }
    }
}
