package com.reedelk.rest.component;

import com.reedelk.rest.client.*;
import com.reedelk.rest.commons.IsNotSuccessful;
import com.reedelk.rest.configuration.RestClientConfiguration;
import com.reedelk.rest.configuration.RestMethod;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.ByteArrayContent;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.service.ScriptEngineService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
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

        final ResponseData dataHolder = new ResponseData();
        Mono<byte[]> bytesMono = client.execute(uri, new BodyProvider() {
            @Override
            public BodyProviderData data() {
                // TODO: Take the body and interpret it..(might be javascript)
                // Request body has to be provided if and only if it is a POST,PUT,DELETE.
                // Also if the body is null, don't bother to do anything, just
                // send empty byte array buffer.
                // If the body is already a stream, then we just stream it upstream. (we support stream outbound)
                byte[] bodyAsBytes = input.getTypedContent().asByteArray();
                return new BodyProviderData() {
                    @Override
                    public Publisher<? extends ByteBuf> provide() {
                        return Flux.just(Unpooled.wrappedBuffer(bodyAsBytes));
                    }

                    @Override
                    public int length() {
                        return bodyAsBytes.length;
                    }
                };
            }
        },
                (response, byteBufMono) -> {
                    dataHolder.status = response.status();
                    dataHolder.headers = response.responseHeaders();
                    return byteBufMono.asByteArray();
                    // Body provider only if it is POST, PUT ...
                });

        // We block and wait until the whole response has been received
        // note that because of this line, this component is not supporting
        // a stream (inbound), but it does support streaming outbound...
        byte[] bytes = bytesMono.block();

        // If the response is not in the Range 2xx, we throw an exception.
        if (IsNotSuccessful.from(dataHolder.status)) {
            throw new ESBException(dataHolder.status.toString());
        }

        // We set the type of the content according to the
        // Content type header.
        Type type = ExtractTypeFromHeaders.from(dataHolder.headers);

        // We set the content
        TypedContent content = new ByteArrayContent(bytes, type);
        input.setTypedContent(content);
        return input;
    }

    private class ResponseData {
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
