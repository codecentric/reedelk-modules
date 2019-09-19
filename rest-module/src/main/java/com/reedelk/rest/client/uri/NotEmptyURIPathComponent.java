package com.reedelk.rest.client.uri;

import com.reedelk.rest.commons.QueryParams;
import com.reedelk.rest.commons.RemoveQueryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.reedelk.runtime.api.commons.StringUtils.*;
import static java.util.stream.Collectors.joining;


public class NotEmptyURIPathComponent implements URIPathComponent {

    private static final Logger logger = LoggerFactory.getLogger(NotEmptyURIPathComponent.class);

    private static final String UTF_8 = "UTF-8";

    /**
     * Captures URI template variable names.
     */
    private static final Pattern NAMES_PATTERN = Pattern.compile("\\{([^/]+?)\\}");

    private static final String QUERY_PARAM_START = "?";
    private static final String QUERY_PARAM_DELIMITER = "&";

    private final String uri;
    private final String existingQueryParams;

    public NotEmptyURIPathComponent(String path) {
        this.uri = RemoveQueryParams.from(path);
        this.existingQueryParams = encodeExistingQueryParams(QueryParams.of(path));
    }

    @Override
    public String expand(Map<String, String> pathParams, Map<String, String> queryParams) {
        String uriWithExpandedPathParams = PATH_ENCODER.apply(expandPathParams(pathParams));
        return expandQueryParams(uriWithExpandedPathParams, queryParams);
    }

    private String expandQueryParams(String uri, Map<String, String> queryParams) {
        StringBuilder builder = new StringBuilder(uri);
        if (isNotBlank(existingQueryParams)) {
            builder.append(QUERY_PARAM_START)
                    .append(existingQueryParams);
        }
        if (queryParams != null && !queryParams.isEmpty()) {
            if (isNotBlank(existingQueryParams)) {
                builder.append(QUERY_PARAM_DELIMITER);
            } else {
                builder.append(QUERY_PARAM_START);
            }
            builder.append(toQueryParamsString(queryParams));
        }
        return builder.toString();
    }

    private String toQueryParamsString(Map<String, String> queryParams) {
        return queryParams.keySet().stream()
                .map(key -> QUERY_PARAM_ENCODER.apply(key)
                        + "=" +
                        QUERY_PARAM_ENCODER.apply(queryParams.get(key)))
                .collect(joining(QUERY_PARAM_DELIMITER));
    }

    private String expandPathParams(Map<String, String> pathParams) {
        if (uri == null) {
            return null;
        }
        if (uri.indexOf('{') == -1) {
            return uri; // just append
        }
        if (pathParams == null) {
            pathParams = new LinkedHashMap<>();
        }

        Matcher matcher = NAMES_PATTERN.matcher(uri);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group(1);
            String varName = getVariableName(match);
            Object varValue = pathParams.get(varName);

            String formatted = getVariableValueAsString(varValue);
            matcher.appendReplacement(sb, formatted);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String getVariableValueAsString(Object variableValue) {
        return (variableValue != null ? variableValue.toString() : EMPTY);
    }

    private static String getVariableName(String match) {
        int colonIdx = match.indexOf(':');
        return (colonIdx != -1 ? match.substring(0, colonIdx) : match);
    }

    private String encodeExistingQueryParams(String queryParams) {
        if (isBlank(queryParams)) return queryParams;

        String[] queryKeyAndValues = queryParams.split("&");
        Map<String, String> notEncodedQueryParams = new LinkedHashMap<>();

        Arrays.stream(queryKeyAndValues).forEach(keyAndValue -> {
            String[] keyAndValueSegments = keyAndValue.split("=");
            notEncodedQueryParams.put(keyAndValueSegments[0], keyAndValueSegments[1]);
        });
        return toQueryParamsString(notEncodedQueryParams);
    }

    private static final UnaryOperator<String> QUERY_PARAM_ENCODER = original -> {
        try {
            return URLEncoder.encode(original, UTF_8)
                    .replaceAll("\\+", "%20"); // Apparently spaces encoded  as '+' are not good.
        } catch (UnsupportedEncodingException e) {
            logger.warn("UTF-8 not supported", e);
            return original;
        }
    };

    private static final UnaryOperator<String> PATH_ENCODER = PathEncoder::encodePath;

}
