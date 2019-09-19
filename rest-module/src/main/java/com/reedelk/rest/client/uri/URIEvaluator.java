package com.reedelk.rest.client.uri;

import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.NMapEvaluation;
import com.reedelk.runtime.api.service.ScriptEngineService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class URIEvaluator {

    private String baseURL;
    private URIComponent URIComponent;
    private ScriptEngineService scriptEngine;
    private Map<String, String> pathParameters;
    private Map<String, String> queryParameters;

    public URIProvider provider(Message message, FlowContext flowContext) {
        String requestURI = baseURL + evaluateRequestURI(message, flowContext);
        return () -> URI.create(requestURI);
    }

    /**
     * We check which parameters are effectively there to understand what to evaluate.
     * Next optimization could be checking in the map which values
     * are actually scripts and then evaluate only those ones.
     */
    private String evaluateRequestURI(Message message, FlowContext flowContext) {

        if (pathParameters.isEmpty() && queryParameters.isEmpty()) {
            // If path and query parameters are not to be evaluated, when we don't do it.
            return URIComponent.expand(pathParameters, queryParameters);

        } else if (pathParameters.isEmpty()) {
            // Only query parameters are present.
            NMapEvaluation<String> evaluation =
                    scriptEngine.evaluate(message, flowContext, queryParameters);
            Map<String, String> evaluatedQueryParameters = evaluation.map(0);
            return URIComponent.expand(pathParameters, evaluatedQueryParameters);

        } else if (queryParameters.isEmpty()) {
            // Only path parameters are present.
            NMapEvaluation<String> evaluation =
                    scriptEngine.evaluate(message, flowContext, pathParameters);
            Map<String, String> evaluatedPathParameters = evaluation.map(0);
            return URIComponent.expand(evaluatedPathParameters, queryParameters);

        } else {
            // Both path and query parameters are present.
            NMapEvaluation<String> evaluation =
                    scriptEngine.evaluate(message, flowContext, pathParameters, queryParameters);
            Map<String, String> evaluatedPathParameters = evaluation.map(0);
            Map<String, String> evaluatedQueryParameters = evaluation.map(1);
            return URIComponent.expand(evaluatedPathParameters, evaluatedQueryParameters);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String path;
        private String baseURL;
        private ScriptEngineService scriptEngine;
        private ClientConfiguration configuration;
        private Map<String, String> pathParameters;
        private Map<String, String> queryParameters;

        public Builder baseURL(String baseURL) {
            this.baseURL = baseURL;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder scriptEngine(ScriptEngineService scriptEngine) {
            this.scriptEngine = scriptEngine;
            return this;
        }

        public Builder configuration(ClientConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder pathParameters(Map<String, String> pathParameters) {
            this.pathParameters = pathParameters;
            return this;
        }

        public Builder queryParameters(Map<String, String> queryParameters) {
            this.queryParameters = queryParameters;
            return this;
        }

        public URIEvaluator build() {
            URIEvaluator evaluator = new URIEvaluator();
            evaluator.URIComponent = new URIComponent(path);
            evaluator.scriptEngine = scriptEngine;
            evaluator.pathParameters = pathParameters;
            evaluator.queryParameters = queryParameters;

            // Use config
            if (StringUtils.isNull(baseURL)) {
                requireNonNull(configuration, "Expected configuration or BaseURL");

                String host = configuration.getHost();
                Integer port = port(configuration.getPort());
                String basePath = configuration.getBasePath();
                String scheme = scheme(configuration.getProtocol());
                try {
                    URI uri = new URI(scheme, null, host, port, basePath, null, null);
                    evaluator.baseURL = uri.toString();
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Could not build URI", e);
                }
            } else {
                // Use base URL
                evaluator.baseURL = baseURL;
            }
            return evaluator;
        }

        private int port(Integer port) {
            if (port == null) return -1;
            else return port;
        }

        private String scheme(HttpProtocol protocol) {
            return protocol == null ?
                    HttpProtocol.HTTP.toString().toLowerCase() :
                    protocol.toString().toLowerCase();
        }
    }
}
