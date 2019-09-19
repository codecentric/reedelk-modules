package com.reedelk.rest.client;

import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.rest.configuration.client.Proxy;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
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

    private CloseableHttpAsyncClient createClientByBaseURL() {
        return HttpAsyncClients.createDefault();
    }

    private CloseableHttpAsyncClient createClientByConfig(ClientConfiguration configuration) {

        HttpAsyncClientBuilder builder = HttpAsyncClients.custom();


        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        if (Proxy.PROXY.equals(configuration.getProxy())) {

        }
        configureProxy(configuration, builder, credentialsProvider);

        RequestConfig requestConfig = createConfig(configuration);
        return builder
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
    }

    private void configureProxy(ClientConfiguration configuration, HttpAsyncClientBuilder builder, CredentialsProvider credentialsProvider) {
        String theProxyHost = "";
        int theProxyPort = 8889;
        HttpHost proxyHost = new HttpHost(theProxyHost, theProxyPort);
        builder.setProxy(proxyHost);

        String proxyUserName = "";
        String proxyPassword = "";

        // This is done only if password
        credentialsProvider.setCredentials(
                new AuthScope(theProxyHost, theProxyPort),
                new UsernamePasswordCredentials(proxyUserName, proxyPassword));
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

    public void dispose() {
        CONFIG_ID_CLIENT.forEach(this::closeClient);
        BASE_URL_CLIENT.forEach(this::closeClient);
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
