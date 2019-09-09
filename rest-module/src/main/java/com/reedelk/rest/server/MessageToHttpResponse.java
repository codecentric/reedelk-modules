package com.reedelk.rest.server;

import com.reedelk.rest.configuration.RestListenerErrorResponse;
import com.reedelk.rest.configuration.RestListenerResponse;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.api.service.ScriptExecutionResult;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.Map;
import java.util.Optional;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class MessageToHttpResponse {


    public static Publisher<byte[]> from(Throwable exception,
                                         FlowContext flowContext,
                                         HttpServerResponse response,
                                         ScriptEngineService scriptEngineService,
                                         RestListenerErrorResponse errorResponseConfig) {

        HttpResponseStatus responseStatus = EvaluateStatusCode.withDefault(INTERNAL_SERVER_ERROR)
                .withStatus(errorResponseConfig.getStatus())
                .withScriptEngine(scriptEngineService)
                .withThrowable(exception)
                .withContext(flowContext)
                .evaluate();
        response.status(responseStatus);

        addAdditionalHeaders(response.responseHeaders(), errorResponseConfig.getHeaders());

        return Mono.just(exception.getMessage().getBytes());
    }


    public static Publisher<byte[]> from(Message message,
                                         FlowContext flowContext,
                                         HttpServerResponse response,
                                         ScriptEngineService scriptEngineService,
                                         RestListenerResponse responseConfig) {

        HttpResponseStatus responseStatus = EvaluateStatusCode.withDefault(OK)
                .withStatus(responseConfig.getStatus())
                .withScriptEngine(scriptEngineService)
                .withContext(flowContext)
                .withMessage(message)
                .evaluate();
        response.status(responseStatus);

        contentTypeFrom(message, responseConfig).ifPresent(contentType -> response.addHeader(CONTENT_TYPE, contentType));

        addAdditionalHeaders(response.responseHeaders(), responseConfig.getHeaders());

        if (responseConfig.getBody() != null) {
            // Custom body - evaluate script - or just return the value (if it is not a script)
            String scriptBody = responseConfig.getBody();
            try {
                ScriptExecutionResult result = scriptEngineService.evaluate(message, scriptBody, new ComponentVariableBindings(flowContext));
                Object object = result.getObject();
                return Mono.just(object.toString().getBytes());
            } catch (ScriptException e) {
                return Mono.just(e.getMessage().getBytes());
            }

        } else {
            // The content type comes from the message typed content
            TypedContent<?> typedContent = message.getTypedContent();
            return typedContent.asByteArrayStream();
        }
    }

    static class ComponentVariableBindings extends SimpleBindings {
        ComponentVariableBindings(FlowContext flowContext) {
        }
    }

    /**
     * For each additional header, if present in the current headers it gets replaced,
     * otherwise it is  added to the current headers collection.
     *
     * @param currentHeaders    the current headers.
     * @param additionalHeaders additional user defined headers.
     */
    private static void addAdditionalHeaders(HttpHeaders currentHeaders, Map<String, String> additionalHeaders) {
        additionalHeaders.forEach((headerName, headerValue) -> {
            Optional<String> optionalMatchingHeaderName = matchingHeader(currentHeaders, headerName);
            if (optionalMatchingHeaderName.isPresent()) {
                String matchingHeaderName = optionalMatchingHeaderName.get();
                currentHeaders.remove(matchingHeaderName);
                currentHeaders.add(headerName.toLowerCase(), headerValue);
            } else {
                currentHeaders.add(headerName.toLowerCase(), headerValue);
            }
        });
    }

    private static Optional<String> contentTypeFrom(Message message, RestListenerResponse responseConfig) {
        // If the content type is a custom body, the developer MUST define the content type in the response config.
        if (responseConfig.getBody() == null) {
            // Then we use the content type from the payload's mime type.
            TypedContent<?> typedContent = message.getTypedContent();
            Type type = typedContent.type();
            MimeType contentType = type.getMimeType();
            return Optional.of(contentType.toString());
        }
        return Optional.empty();
    }

    // Returns the matching header name
    private static Optional<String> matchingHeader(HttpHeaders headers, String targetHeaderName) {
        for (String headerName : headers.names()) {
            if (headerName.toLowerCase().equals(targetHeaderName.toLowerCase())) {
                return Optional.of(headerName);
            }
        }
        return Optional.empty();
    }
}
