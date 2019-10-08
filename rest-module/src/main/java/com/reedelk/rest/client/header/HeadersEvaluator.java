package com.reedelk.rest.client.header;

import com.reedelk.rest.client.authentication.BasicAuthentication;
import com.reedelk.rest.commons.ContentType;
import com.reedelk.rest.commons.IsMessagePayload;
import com.reedelk.rest.configuration.client.*;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.dynamicmap.DynamicStringMap;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.apache.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.reedelk.rest.commons.ConfigPreconditions.requireNotNull;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.rest.commons.Messages.RestClient.PROXY_CONFIG_MISSING;

public class HeadersEvaluator {

    private ScriptEngineService scriptEngine;
    private DynamicStringMap userHeaders;
    private DynamicByteArray body;

    private String proxyPreemptiveAuthHeaderValue;

    private HeadersEvaluator(ScriptEngineService scriptEngine,
                             ClientConfiguration configuration,
                             DynamicStringMap userHeaders,
                             DynamicByteArray body) {
        this.scriptEngine = scriptEngine;
        this.userHeaders = userHeaders;
        this.body = body;

        configurePreemptiveProxyAuthentication(configuration);
    }

    public HeaderProvider provider(Message message, FlowContext flowContext) {
        Map<String, String> headers = new HashMap<>();

        if (IsMessagePayload.from(body)) {
            ContentType.from(message)
                    .ifPresent(contentType -> headers.put(CONTENT_TYPE, contentType));
        }

        if (proxyPreemptiveAuthHeaderValue != null) {
            headers.put(HttpHeaders.PROXY_AUTHORIZATION, proxyPreemptiveAuthHeaderValue);
        }

        if (!userHeaders.isEmpty()) {
            // User-defined headers: interpret and add them
            Map<String, String> evaluatedHeaders = scriptEngine.evaluate(userHeaders, message, flowContext);
            headers.putAll(evaluatedHeaders);
        }

        return () -> headers;
    }

    private void configurePreemptiveProxyAuthentication(ClientConfiguration configuration) {
        Optional.ofNullable(configuration).ifPresent(config -> {
            Proxy proxy = config.getProxy();
            if (Proxy.PROXY.equals(proxy)) {
                ProxyConfiguration proxyConfig =
                        requireNotNull(config.getProxyConfiguration(), PROXY_CONFIG_MISSING.format());
                ProxyAuthentication proxyAuth = proxyConfig.getAuthentication();
                if (ProxyAuthentication.BASIC.equals(proxyAuth)) {
                    ProxyAuthenticationConfiguration authConfig = proxyConfig.getAuthenticationConfiguration();
                    if (Boolean.TRUE.equals(authConfig.getPreemptive())) {
                        BasicAuthentication basicAuthentication =
                                new BasicAuthentication(authConfig.getUsername(), authConfig.getPassword());
                        proxyPreemptiveAuthHeaderValue = basicAuthentication.authenticationHeader();
                    }
                }
            }
        });
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ClientConfiguration configuration;
        private ScriptEngineService scriptEngine;
        private DynamicStringMap headers;
        private DynamicByteArray body;

        public Builder configuration(ClientConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder scriptEngine(ScriptEngineService scriptEngine) {
            this.scriptEngine = scriptEngine;
            return this;
        }

        public Builder headers(DynamicStringMap headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(DynamicByteArray body) {
            this.body = body;
            return this;
        }

        public HeadersEvaluator build() {
            return new HeadersEvaluator(scriptEngine, configuration, headers, body);
        }
    }
}
