package com.reedelk.rest.client;

import com.reedelk.rest.client.strategy.ExecutionStrategy;
import com.reedelk.rest.client.strategy.ResponseHandler;
import com.reedelk.rest.client.strategy.StrategyBuilder;
import com.reedelk.rest.configuration.RestMethod;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;

import java.util.function.BiConsumer;

public class HttpClientWrapper implements ExecutionStrategy {

    private HttpClient client;
    private RestMethod method;
    private ExecutionStrategy delegate;

    private String baseURL;
    private boolean followRedirects;


    HttpClientWrapper() {
        this.client = HttpClient.create();
    }

    void port(int port) {
        client = client.port(port);
    }

    void baseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    void method(RestMethod method) {
        this.method = method;
    }

    void compress(boolean compress) {
        client = client.compress(compress);
    }

    void keepAlive(boolean keepAlive) {
        client = client.keepAlive(keepAlive);
    }

    void followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    void doOnRequest(BiConsumer<HttpClientRequest, Connection> handler) {
        client = client.doOnRequest(handler);
    }

    void initialize() {
        delegate = StrategyBuilder.get()
                .client(client)
                .method(method)
                .baseURL(baseURL)
                .followRedirects(followRedirects)
                .build();
    }

    @Override
    public <T> Mono<T> execute(String uri, BodyProvider bodyProvider, ResponseHandler<T> handler) {
        return delegate.execute(uri, bodyProvider, handler);
    }


    /**
     * void proxy(int proxy) {
     * client = client.tcpConfiguration(new Function<TcpClient, TcpClient>() {
     *
     * @Override public TcpClient apply(TcpClient tcpClient) {
     * return tcpClient.proxy(new Consumer<ProxyProvider.TypeSpec>() {
     * @Override public void accept(ProxyProvider.TypeSpec typeSpec) {
     * typeSpec.type(ProxyProvider.Proxy.HTTP)
     * .host("localhost")
     * .port(123)
     * .username("user")
     * .password(new Function<String, String>() {
     * @Override public String apply(String s) {
     * return "sadf";
     * }
     * });
     * }
     * });
     * }
     * });
     * }
     */
}
