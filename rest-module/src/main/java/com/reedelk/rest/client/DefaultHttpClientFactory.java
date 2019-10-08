package com.reedelk.rest.client;

import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.rest.configuration.client.*;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.util.Optional;

import static java.lang.Boolean.TRUE;

public class DefaultHttpClientFactory implements HttpClientFactory {

    @Override
    public HttpClient from(ClientConfiguration config) {
        HttpAsyncClientBuilder builder = HttpAsyncClients.custom();

        HttpClientContext context = HttpClientContext.create();

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        // Request config
        RequestConfig requestConfig = createConfig(config);

        // Basic authentication config
        Authentication authentication = config.getAuthentication();
        if (Authentication.BASIC.equals(authentication)) {
            configureBasicAuth(
                    config.getHost(),
                    config.getPort(),
                    config.getProtocol(),
                    config.getBasicAuthentication(),
                    credentialsProvider,
                    context);
        }

        // Digest authentication config
        if (Authentication.DIGEST.equals(authentication)) {
            configureDigestAuth(
                    config.getHost(),
                    config.getPort(),
                    config.getProtocol(),
                    config.getDigestAuthentication(),
                    credentialsProvider,
                    context);
        }

        // Proxy config
        Proxy proxy = config.getProxy();
        if (Proxy.PROXY.equals(proxy)) {
            configureProxy(
                    config.getProxyConfiguration(),
                    builder,
                    credentialsProvider,
                    context);
        }

        CloseableHttpAsyncClient client = builder
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();

        return new HttpClient(client, context);
    }

    @Override
    public HttpClient from(String baseURL) {
        return new HttpClient(HttpAsyncClients.createDefault());
    }

    private void configureBasicAuth(String host, int port, HttpProtocol protocol, BasicAuthenticationConfiguration basicAuthConfig, CredentialsProvider credentialsProvider, HttpClientContext context) {
        HttpHost basicAuthHost = new HttpHost(host, port, protocol.name());
        credentialsProvider.setCredentials(
                new AuthScope(basicAuthHost.getHostName(), basicAuthHost.getPort()),
                new UsernamePasswordCredentials(basicAuthConfig.getUsername(), basicAuthConfig.getPassword()));

        if (TRUE.equals(basicAuthConfig.getPreemptive())) {
            AuthCache authCache = new BasicAuthCache();
            authCache.put(basicAuthHost, new BasicScheme());
            context.setAuthCache(authCache);
        }
    }

    private void configureDigestAuth(String host, Integer port, HttpProtocol protocol, DigestAuthenticationConfiguration digestAuthConfig, CredentialsProvider credentialsProvider, HttpClientContext context) {
        HttpHost digestAuthHost = new HttpHost(host, port, protocol.name());
        credentialsProvider.setCredentials(
                new AuthScope(digestAuthHost.getHostName(), digestAuthHost.getPort()),
                new UsernamePasswordCredentials(digestAuthConfig.getUsername(), digestAuthConfig.getPassword()));

        if (TRUE.equals(digestAuthConfig.getPreemptive())) {
            AuthCache authCache = new BasicAuthCache();
            DigestScheme digestAuth = new DigestScheme();
            // Realm and nonce are mandatory in order to compute
            // the Digest auth header when preemptive is expected.
            digestAuth.overrideParamter("realm", digestAuthConfig.getRealm());
            digestAuth.overrideParamter("nonce", digestAuthConfig.getNonce());
            authCache.put(digestAuthHost, digestAuth);
            context.setAuthCache(authCache);
        }
    }

    private void configureProxy(ProxyConfiguration proxyConfig, HttpAsyncClientBuilder builder, CredentialsProvider credentialsProvider, HttpClientContext context) {
        HttpHost proxyHost = new HttpHost(proxyConfig.getHost(), proxyConfig.getPort());
        builder.setProxy(proxyHost);

        if (ProxyAuthentication.USER_AND_PASSWORD.equals(proxyConfig.getAuthentication())) {
            ProxyAuthenticationConfiguration authConfig = proxyConfig.getAuthenticationConfiguration();
            credentialsProvider.setCredentials(
                    new AuthScope(proxyConfig.getHost(), proxyConfig.getPort()),
                    new UsernamePasswordCredentials(authConfig.getUsername(), authConfig.getPassword()));

            if (TRUE.equals(authConfig.getPreemptive())) {
                AuthCache authCache = new BasicAuthCache();
                authCache.put(proxyHost, new BasicScheme());
                context.setAuthCache(authCache);
            }
        }
    }

    private RequestConfig createConfig(ClientConfiguration configuration) {
        RequestConfig.Builder builder = RequestConfig.custom();

        Optional.ofNullable(configuration.getFollowRedirects())
                .ifPresent(isFollowRedirects -> {
                    builder.setRedirectsEnabled(isFollowRedirects);
                    builder.setCircularRedirectsAllowed(isFollowRedirects);
                    builder.setRelativeRedirectsAllowed(isFollowRedirects);
                });

        Optional.ofNullable(configuration.getExpectContinue())
                .ifPresent(builder::setExpectContinueEnabled);

        Optional.ofNullable(configuration.getRequestTimeout())
                .ifPresent(builder::setConnectionRequestTimeout);

        Optional.ofNullable(configuration.getConnectTimeout())
                .ifPresent(builder::setConnectTimeout);

        return builder.build();
    }
}
