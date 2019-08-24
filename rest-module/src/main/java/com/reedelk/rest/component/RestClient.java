package com.reedelk.rest.component;

import com.reedelk.rest.client.ResponseReceiverBuilder;
import com.reedelk.rest.client.UriComponent;
import com.reedelk.rest.configuration.RestCallerConfiguration;
import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.TabGroup;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.ByteArrayStreamType;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.service.ScriptEngineService;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;

import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;
import static reactor.netty.http.client.HttpClient.ResponseReceiver;

@ESBComponent("REST Client")
@Component(service = RestClient.class, scope = PROTOTYPE)
public class RestClient implements ProcessorSync {

    @Reference
    private ScriptEngineService service;

    @Property("Path")
    @Default("/")
    private String path;

    @Property("Method")
    private RestMethod method;

    @Property("Body")
    @Default("payload")
    private String body;

    @Property("Configuration")
    private RestCallerConfiguration configuration;

    @TabGroup("Headers and parameters")
    @Property("Headers")
    private Map<String, String> headers;

    @TabGroup("Headers and parameters")
    @Property("Path params")
    private Map<String, String> uriParameters;

    @TabGroup("Headers and parameters")
    @Property("Query params")
    private Map<String, String> queryParameters;

    private volatile ResponseReceiver<?> client;

    private UriComponent uriComponent;

    @Override
    public Message apply(Message input) {

        HttpClient.ResponseReceiver<?> receiver = getClient();

        String uri = buildUri();
        Flux<byte[]> bytes = receiver.uri(uri).response((response, byteBufFlux) -> {
            // Set headers  and status to the message data...
            HttpHeaders entries = response.responseHeaders();
            HttpResponseStatus status = response.status();
            // e.g message.setStatus blab bla

            // Extract message data
            return byteBufFlux.asByteArray();
        });

        TypedContent content = new ByteArrayStreamType(bytes, new Type(MimeType.APPLICATION_JSON));
        input.setTypedContent(content);
        return input;
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

    public void setConfiguration(RestCallerConfiguration configuration) {
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

    private ResponseReceiver getClient() {
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

    private ResponseReceiver createClient() {
        return ResponseReceiverBuilder.get()
                .method(method)
                .port(configuration.getPort())
                .host(configuration.getHost())
                .baseUrl(configuration.getBasePath())
                .keepAlive(configuration.getPersistentConnections())
                .followRedirects(configuration.getFollowRedirects())
                .responseBufferSize(configuration.getResponseBufferSize())
                .connectionIdleTimeout(configuration.getConnectionIdleTimeout())
                .onRequestConsumer((request, connection) -> interpretAndAddHeaders(request))
                .build();
    }

    private void interpretAndAddHeaders(HttpClientRequest request) {
        // Interpret headers and add them to the request.
        request.addHeader("Content-Type", "application/json");
    }
}
