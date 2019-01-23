package com.esb.fonnel.processor.http.inbound;


import com.esb.foonnel.domain.AbstractInbound;
import com.esb.foonnel.domain.Message;
import com.esb.foonnel.domain.Processor;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.osgi.service.component.annotations.Component;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.ipc.netty.http.server.HttpServerRequest;
import reactor.ipc.netty.http.server.HttpServerResponse;

import java.util.HashMap;
import java.util.function.BiFunction;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(scope = PROTOTYPE, service = HttpListenerInbound.class)
public class HttpListenerInbound extends AbstractInbound {


    private String path;
    private String method;
    private int port;
    private String bindAddress;

    private HttpServer server;


    public void onStart() {
        reactor.ipc.netty.http.server.HttpServer build = reactor.ipc.netty.http.server.HttpServer.builder()
                .port(port)
                .bindAddress(bindAddress)
                .build();
        build.start(new ServerConnectionHandler(null));
    }

    public void onShutdown() {
        server.removeRoute(method, path);
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    class ServerConnectionHandler implements BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> {

        private Processor listener;

        ServerConnectionHandler(Processor listener) {
            this.listener = listener;
        }

        @Override
        public Publisher<Void> apply(HttpServerRequest request, HttpServerResponse response) {

            Message payload = from(request);
            Flux<Message> messageFlux = Flux.just(payload);


                Message message = new Message();
                message.setContent("asdfasdfad");


            onEvent(message);



            if (HttpMethod.POST.equals(request.method())) {
                messageFlux = request.receive().asByteArray()
                        .map(bytes -> {
                            payload.setContent(new String(bytes));
                            return payload;
                        });
            }

            onEvent(payload);

/**
 return messageFlux.transform(listener)
 .flatMap(message -> {
 Map<String, String> httpHeaders = message.getHttpHeaders();
 httpHeaders.forEach(response::addHeader);
 response.status(message.getHttpStatus());
 return response.sendByteArray(message.byteStream());
 });*/

            response.status(HttpResponseStatus.OK);
            return response.sendString(Flux.just("ciao"));
        }

    }

    private Message from(HttpServerRequest request) {
        Message payload = new Message();
        payload.setRequestPath(request.path());
        payload.setRequestMethod(request.method().name());
        HttpHeaders entries = request.requestHeaders();
        payload.setRequestHeaders(new HashMap<>());
        payload.setRequestParams(new QueryStringDecoder(request.uri()).parameters());
        return payload;
    }

}
