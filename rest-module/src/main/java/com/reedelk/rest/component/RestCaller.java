package com.reedelk.rest.component;

import com.reedelk.rest.client.ResponseReceiverBuilder;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.RestCallerConfiguration;
import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
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
import reactor.netty.http.client.HttpClientRequest;

import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;
import static reactor.netty.http.client.HttpClient.ResponseReceiver;

@ESBComponent("REST Client")
@Component(service = RestCaller.class, scope = PROTOTYPE)
public class RestCaller implements ProcessorSync {

    @Reference
    private ScriptEngineService service;

    @Property("Path")
    @Default("/")
    private String path;

    @Property("Method")
    @Default("GET")
    private RestMethod method;

    @Property("Body")
    @Default("payload")
    private String body;

    @Property("Headers")
    private Map<String,String> headers;

    @Property("Query parameters")
    private Map<String,String> queryParameters;

    @Property("URI parameters")
    private Map<String,String> uriParameters;

    @Property("Configuration")
    private RestCallerConfiguration configuration;

    @Property("Follow redirects")
    private Boolean followRedirects;


    private volatile ResponseReceiver<?> client;

    @Override
    public Message apply(Message input) {

        ResponseReceiver<?> receiver = getClient();

        Flux<byte[]> bytes = receiver.uri(interpretPath(path)).response((response, byteBufFlux) -> {
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

    /**
     * Interpret  path dynamic values and  add query parameters.
     */
    private String interpretPath(String path) {
        return null;
    }

    private ResponseReceiver getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = createClient();
                }
            }
        }
        return client;
    }

    private ResponseReceiver createClient() {
        return ResponseReceiverBuilder.get()
                .method(method)
                .followRedirects(followRedirects)
                .port(configuration.getPort())
                .host(configuration.getHost())
                .baseUrl(configuration.getBasePath())
                .keepAlive(configuration.isPersistentConnections())
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
