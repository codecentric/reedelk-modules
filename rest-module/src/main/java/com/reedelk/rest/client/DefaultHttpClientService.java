package com.reedelk.rest.client;

import com.reedelk.rest.configuration.client.*;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Boolean.TRUE;

public class DefaultHttpClientService implements HttpClientService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHttpClientService.class);

    private Map<String, HttpClient> CONFIG_ID_CLIENT = new HashMap<>();
    private Map<String, HttpClient> BASE_URL_CLIENT = new HashMap<>();

    @Override
    public HttpClient clientByConfig(ClientConfiguration configuration) {
        String configId = configuration.getId();
        if (!CONFIG_ID_CLIENT.containsKey(configId)) {
            synchronized (this) {
                if (!CONFIG_ID_CLIENT.containsKey(configId)) {
                    HttpClient client = createClientByConfig(configuration);
                    client.start();
                    CONFIG_ID_CLIENT.put(configId, client);
                }
            }
        }
        return CONFIG_ID_CLIENT.get(configId);
    }

    @Override
    public HttpClient clientByBaseURL(String baseURL) {
        if (!BASE_URL_CLIENT.containsKey(baseURL)) {
            synchronized (this) {
                if (!BASE_URL_CLIENT.containsKey(baseURL)) {
                    HttpClient client = createClientByBaseURL();
                    client.start();
                    BASE_URL_CLIENT.put(baseURL, client);
                }
            }
        }
        return BASE_URL_CLIENT.get(baseURL);
    }

    @Override
    public void dispose() {
        CONFIG_ID_CLIENT.forEach(this::closeClient);
        BASE_URL_CLIENT.forEach(this::closeClient);
    }

    private void closeClient(String key, HttpClient client) {
        try {
            client.close();
        } catch (Exception exception) {
            logger.error(String.format("error closing http client for key=%s", key), exception);
        }
    }

    private HttpClient createClientByBaseURL() {
        return new HttpClient(HttpAsyncClients.createDefault());
    }

    private HttpClient createClientByConfig(ClientConfiguration configuration) {

        HttpAsyncClientBuilder builder = HttpAsyncClients.custom();

        HttpClientContext context = HttpClientContext.create();

        CredentialsProvider credentialsProvider = new SystemDefaultCredentialsProvider();


        // Request config
        RequestConfig requestConfig = createConfig(configuration);

        // Authentication config
        Authentication authentication = configuration.getAuthentication();
        if (Authentication.BASIC.equals(authentication)) {
            configureBasicAuth(
                    configuration.getHost(),
                    configuration.getPort(),
                    configuration.getBasicAuthentication(),
                    credentialsProvider, context);
        }
        if (Authentication.DIGEST.equals(authentication)) {
            configureDigestAuth(
                    configuration.getHost(),
                    configuration.getPort(),
                    configuration.getDigestAuthentication(),
                    credentialsProvider, context);
        }

        // Proxy config
        Proxy proxy = configuration.getProxy();
        if (Proxy.PROXY.equals(proxy)) {
            configureProxy(configuration.getProxyConfiguration(), builder, credentialsProvider);
        }

        CloseableHttpAsyncClient client = builder
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();

        return new HttpClient(client, context);
    }

    private void configureDigestAuth(String host, Integer port, DigestAuthenticationConfiguration digestAuthConfig, CredentialsProvider credentialsProvider, HttpClientContext context) {
        credentialsProvider.setCredentials(
                new AuthScope(
                        AuthScope.ANY_HOST,
                        AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(
                        digestAuthConfig.getUsername(),
                        digestAuthConfig.getPassword()));

        if (TRUE.equals(digestAuthConfig.getPreemptive())) {
            AuthCache authCache = new BasicAuthCache();
            HttpHost authHost = new HttpHost(host, port);
            authCache.put(authHost, new BasicScheme());
            context.setAuthCache(authCache);
        }
    }

    private void configureBasicAuth(String host, int port, BasicAuthenticationConfiguration basicAuthConfig, CredentialsProvider credentialsProvider, HttpClientContext context) {
        credentialsProvider.setCredentials(
                new AuthScope(
                        AuthScope.ANY_HOST,
                        AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(
                        basicAuthConfig.getUsername(),
                        basicAuthConfig.getPassword()));

        if (TRUE.equals(basicAuthConfig.getPreemptive())) {
            AuthCache authCache = new BasicAuthCache();
            HttpHost authHost = new HttpHost(host, port);
            authCache.put(authHost, new BasicScheme());
            context.setAuthCache(authCache);
        }
    }

    private void configureProxy(ProxyConfiguration proxyConfiguration, HttpAsyncClientBuilder builder, CredentialsProvider credentialsProvider) {
        HttpHost proxyHost = new HttpHost(
                proxyConfiguration.getHost(),
                proxyConfiguration.getPort());
        builder.setProxy(proxyHost);
        if (ProxyAuthentication.USER_AND_PASSWORD.equals(proxyConfiguration.getAuthentication())) {
            ProxyAuthenticationConfiguration authenticationConfig = proxyConfiguration.getAuthenticationConfiguration();
            credentialsProvider.setCredentials(
                    new AuthScope(
                            proxyConfiguration.getHost(),
                            proxyConfiguration.getPort()),
                    new UsernamePasswordCredentials(
                            authenticationConfig.getUsername(),
                            authenticationConfig.getPassword()));
        }
    }

    private RequestConfig createConfig(ClientConfiguration configuration) {

        RequestConfig.Builder builder = RequestConfig.custom();

        Boolean followRedirects = configuration.getFollowRedirects();
        if (followRedirects != null) {
            builder.setRedirectsEnabled(followRedirects);
            builder.setCircularRedirectsAllowed(followRedirects);
            builder.setRelativeRedirectsAllowed(followRedirects);
        }
        Boolean contentCompression = configuration.getContentCompression();
        if (contentCompression != null) {
            builder.setContentCompressionEnabled(contentCompression);
        }

        Boolean expectContinue = configuration.getExpectContinue();
        if (expectContinue != null) {
            builder.setExpectContinueEnabled(expectContinue);
        }

        Integer connectionRequestTimeout = configuration.getRequestTimeout();
        if (connectionRequestTimeout != null) {
            builder.setConnectionRequestTimeout(connectionRequestTimeout);
        }

        Integer connectTimeout = configuration.getConnectTimeout();
        if (connectTimeout != null) {
            builder.setConnectTimeout(connectTimeout);
        }

        return builder.build();
    }
}
