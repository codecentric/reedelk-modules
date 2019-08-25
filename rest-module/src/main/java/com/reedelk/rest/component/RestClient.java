package com.reedelk.rest.component;

import com.reedelk.rest.client.Client;
import com.reedelk.rest.client.ResponseReceiverBuilder;
import com.reedelk.rest.client.UriComponent;
import com.reedelk.rest.configuration.RestCallerConfiguration;
import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
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
    private RestCallerConfiguration configuration;

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

    private volatile Client client;

    private UriComponent uriComponent;

    @Override
    public Message apply(Message input) {

        Client client = getClient();

        String uri = buildUri();
        try {
            Flux<byte[]> bytes = client.execute(uri, (response, byteBufFlux) -> {
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

        } catch (Exception e) {
            throw new ESBException(e);
        }


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

    private Client getClient() {
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

    private Client createClient() {
        return ResponseReceiverBuilder.get()
                .method(method)
                .port(configuration.getPort())
                .host(configuration.getHost())
                .protocol(configuration.getProtocol())
                .basePath(configuration.getBasePath())
                .keepAlive(configuration.getPersistentConnections())
                .followRedirects(configuration.getFollowRedirects())
                .onRequestConsumer((request, connection) -> interpretAndAddHeaders(request))
                .build();
    }

    private void interpretAndAddHeaders(HttpClientRequest request) {
        // Interpret headers
        if (headers != null) {
            headers.forEach(request::addHeader);
        }
    }
}
