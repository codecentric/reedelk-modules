package com.reedelk.rest.server.mapper;

import com.reedelk.rest.configuration.listener.ListenerErrorResponse;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

import java.util.Map;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class MessageHttpResponseMapper {

    private final ListenerErrorResponse errorResponse;
    private final Map<String, String> responseHeaders;
    private final ScriptEngineService scriptEngine;
    private final Boolean useErrorResponse;
    private final String responseStatus;
    private final String responseBody;

    public MessageHttpResponseMapper(
            ScriptEngineService scriptEngine,
            String responseBody,
            String responseStatus,
            Map<String, String> responseHeaders,
            Boolean useErrorResponse,
            ListenerErrorResponse errorResponse) {
        this.useErrorResponse = useErrorResponse;
        this.responseHeaders = responseHeaders;
        this.responseStatus = responseStatus;
        this.errorResponse = errorResponse;
        this.scriptEngine = scriptEngine;
        this.responseBody = responseBody;
    }

    /**
     * Maps an out Message to a successful http response. It returns the byte stream
     * to be sent back to the client. This mapper DOES HAVE side effects on the HTTP
     * server response object.
     *
     * @param message     out flow Message
     * @param response    http response to be sent back to the client
     * @param flowContext the flow context object holding flow variables and other contextual info
     * @return a stream of bytes representing the response to be sent back to the client.
     */
    public Publisher<byte[]> map(Message message, HttpServerResponse response, FlowContext flowContext) {
        // Map status code
        HttpResponseStatus status =
                EvaluateStatusCode.withDefault(OK)
                        .withScriptEngine(scriptEngine)
                        .withStatus(responseStatus)
                        .withContext(flowContext)
                        .withMessage(message)
                        .evaluate();
        response.status(status);

        // Map content type
        ContentType.from(responseBody, message)
                .ifPresent(contentType -> response.addHeader(CONTENT_TYPE, contentType));

        // Map additional headers
        AdditionalHeader.addAll(response, responseHeaders);

        // Map content body
        return EvaluateResponseBody.withResponseBody(responseBody)
                .withScriptEngine(scriptEngine)
                .withContext(flowContext)
                .withMessage(message)
                .evaluate();
    }

    /**
     * Maps an exception to a not successful http response. It returns the byte stream
     * to be sent back to the client. This mapper DOES HAVE side effects on the HTTP
     * server response object.
     *
     * @param exception   the exception we want to map to the HTTP server response
     * @param response    http response to be sent back to the client
     * @param flowContext the flow context object holding flow variables and other contextual info
     * @return a stream of bytes representing the response to be sent back to the client.
     */
    public Publisher<byte[]> map(Throwable exception, HttpServerResponse response, FlowContext flowContext) {
        // Map status code
        HttpResponseStatus responseStatus =
                EvaluateStatusCode.withDefault(INTERNAL_SERVER_ERROR)
                        .withStatus(errorResponse.getStatus())
                        .withScriptEngine(scriptEngine)
                        .withThrowable(exception)
                        .withContext(flowContext)
                        .evaluate();
        response.status(responseStatus);

        // Map content type
        ContentType.from(errorResponse.getBody(), exception)
                .ifPresent(contentType -> response.addHeader(CONTENT_TYPE, contentType));

        //  Map additional headers
        AdditionalHeader.addAll(response, errorResponse.getHeaders());

        // Map content body
        return Mono.just(exception.getMessage().getBytes());
    }

}
