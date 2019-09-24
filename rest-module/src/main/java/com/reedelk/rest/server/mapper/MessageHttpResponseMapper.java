package com.reedelk.rest.server.mapper;

import com.reedelk.rest.commons.Evaluate;
import com.reedelk.rest.configuration.listener.ErrorResponse;
import com.reedelk.rest.configuration.listener.Response;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.script.DynamicMap;
import com.reedelk.runtime.api.script.DynamicValue;
import com.reedelk.runtime.api.service.ScriptEngineService;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerResponse;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class MessageHttpResponseMapper {

    private final ScriptEngineService scriptEngine;
    private final ErrorResponse errorResponse;
    private final Response response;

    public MessageHttpResponseMapper(ScriptEngineService scriptEngine, Response response, ErrorResponse errorResponse) {
        this.response = response;
        this.errorResponse = errorResponse;
        this.scriptEngine = scriptEngine;
    }

    /**
     * Maps an out Message to a successful http response. It returns the byte stream
     * to be sent back to the client. This mapper DOES HAVE side effects on the HTTP
     * server response object.
     *
     * @param message     out flow Message
     * @param serverResponse    http response to be sent back to the client
     * @param flowContext the flow context object holding flow variables and other contextual info
     * @return a stream of bytes representing the response to be sent back to the client.
     */
    public Publisher<byte[]> map(Message message, HttpServerResponse serverResponse, FlowContext flowContext) {
        DynamicValue responseStatus = response != null ? response.getStatus() : null;
        DynamicValue responseBody = response != null ? response.getBody() : ScriptUtils.EVALUATE_PAYLOAD;
        DynamicMap<String> responseHeaders = response != null ? response.getHeaders() : null;

        // Map status code
        HttpResponseStatus status =
                EvaluateStatusCode.withDefault(OK)
                        .withScriptEngine(scriptEngine)
                        .withStatus(responseStatus)
                        .withContext(flowContext)
                        .withMessage(message)
                        .evaluate();
        serverResponse.status(status);

        // Map content type
        ContentType.from(responseBody, message)
                .ifPresent(contentType -> serverResponse.addHeader(CONTENT_TYPE, contentType));

        // Map additional headers
        AdditionalHeader.addAll(serverResponse, responseHeaders);

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
     * @param serverResponse    http response to be sent back to the client
     * @param flowContext the flow context object holding flow variables and other contextual info
     * @return a stream of bytes representing the response to be sent back to the client.
     */
    public Publisher<byte[]> map(Throwable exception, HttpServerResponse serverResponse, FlowContext flowContext) {
        DynamicValue responseStatus = errorResponse != null ? errorResponse.getStatus() : null;
        DynamicValue responseBody = errorResponse != null ? errorResponse.getBody() : Evaluate.ERROR;
        DynamicMap<String> responseHeaders = errorResponse != null ? errorResponse.getHeaders() : null;

        // Map status code
        HttpResponseStatus status =
                EvaluateStatusCode.withDefault(INTERNAL_SERVER_ERROR)
                        .withScriptEngine(scriptEngine)
                        .withStatus(responseStatus)
                        .withThrowable(exception)
                        .withContext(flowContext)
                        .evaluate();
        serverResponse.status(status);

        // note that the content type header is not set if the response body is null.
        if (responseBody != null && responseBody.isNotNull()) {
            // Content type is by default text. If the user wants to output JSON
            // they must override with specific additional headers the content type.
            serverResponse.addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
        }

        //  Map additional headers
        AdditionalHeader.addAll(serverResponse, responseHeaders);

        // Map content body
        return EvaluateErrorResponseBody.withResponseBody(responseBody)
                .withScriptEngine(scriptEngine)
                .withThrowable(exception)
                .withContext(flowContext)
                .evaluate();
    }
}
