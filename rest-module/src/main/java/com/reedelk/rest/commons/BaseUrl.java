package com.reedelk.rest.commons;

import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.exception.ESBException;

import java.net.URI;
import java.net.URISyntaxException;

import static com.reedelk.rest.commons.Preconditions.requireNotBlank;
import static com.reedelk.rest.commons.Preconditions.requireNotNull;
import static com.reedelk.runtime.api.commons.StringUtils.isNotBlank;

public class BaseUrl {

    private BaseUrl() {
    }

    public static String from(ClientConfiguration configuration) {
        String basePath = configuration.getBasePath();
        String host = requireNotBlank(configuration.getHost(), "'Host' must not be empty");
        HttpProtocol protocol = requireNotNull(configuration.getProtocol(), "'Protocol' must not be null");

        String realHost = host;
        if (host.startsWith("http")) {
            realHost = getHost(host);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(protocol.name().toLowerCase())
                .append("://")
                .append(realHost);
        if (isNotBlank(basePath)) {
            builder.append(basePath);
        }
        return builder.toString();
    }

    private static String getHost(String host) {
        try {
            URI uri = new URI(host);
            return uri.getHost();
        } catch (URISyntaxException e) {
            throw new ESBException(e);
        }
    }
}
