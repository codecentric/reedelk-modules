package com.reedelk.rest.client;

import com.reedelk.rest.configuration.client.*;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ApacheClientHttpService implements HttpClientService {

    private static final Logger logger = LoggerFactory.getLogger(ApacheClientHttpService.class);

    private Map<String, CloseableHttpAsyncClient> CONFIG_ID_CLIENT = new HashMap<>();
    private Map<String, CloseableHttpAsyncClient> BASE_URL_CLIENT = new HashMap<>();

    @Override
    public HttpAsyncClient clientByConfig(ClientConfiguration configuration) {
        String configId = configuration.getId();
        if (!CONFIG_ID_CLIENT.containsKey(configId)) {
            synchronized (this) {
                if (!CONFIG_ID_CLIENT.containsKey(configId)) {
                    CloseableHttpAsyncClient client = createClientByConfig(configuration);
                    client.start();
                    CONFIG_ID_CLIENT.put(configId, client);
                }
            }
        }
        return CONFIG_ID_CLIENT.get(configId);
    }

    @Override
    public HttpAsyncClient clientByBaseURL(String baseURL) {
        if (!BASE_URL_CLIENT.containsKey(baseURL)) {
            synchronized (this) {
                if (!BASE_URL_CLIENT.containsKey(baseURL)) {
                    CloseableHttpAsyncClient client = createClientByBaseURL();
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

    private CloseableHttpAsyncClient createClientByBaseURL() {
        return HttpAsyncClients.createDefault();
    }

    private CloseableHttpAsyncClient createClientByConfig(ClientConfiguration configuration) {

        HttpAsyncClientBuilder builder = HttpAsyncClients.custom();

        CredentialsProvider credentialsProvider = new SystemDefaultCredentialsProvider();

        // Request config
        RequestConfig requestConfig = createConfig(configuration);

        // Authentication config
        Authentication authentication = configuration.getAuthentication();
        if (Authentication.BASIC.equals(authentication)) {
            configureBasicAuth(configuration.getBasicAuthentication(), credentialsProvider);
        }
        if (Authentication.DIGEST.equals(authentication)) {
            configureDigestAuth(configuration.getDigestAuthentication(), credentialsProvider);
        }

        // Proxy config
        Proxy proxy = configuration.getProxy();
        if (Proxy.PROXY.equals(proxy)) {
            configureProxy(configuration.getProxyConfiguration(), builder, credentialsProvider);
        }

        return builder
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
    }

    private void configureDigestAuth(DigestAuthenticationConfiguration digestAuthConfig, CredentialsProvider credentialsProvider) {
        credentialsProvider.setCredentials(
                new AuthScope(
                        AuthScope.ANY_HOST,
                        AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(
                        digestAuthConfig.getUsername(),
                        digestAuthConfig.getPassword()));
    }

    private void configureBasicAuth(BasicAuthenticationConfiguration basicAuthConfig, CredentialsProvider credentialsProvider) {
        credentialsProvider.setCredentials(
                new AuthScope(
                        AuthScope.ANY_HOST,
                        AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(
                        basicAuthConfig.getUsername(),
                        basicAuthConfig.getPassword()));
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

    private void closeClient(String key, CloseableHttpAsyncClient client) {
        try {
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            logger.error(String.format("error closing http client for key=%s", key), e);
        }
    }
}
