package com.reedelk.rest.server.mapper;

import com.reedelk.rest.configuration.listener.ErrorResponse;
import com.reedelk.rest.configuration.listener.Response;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.script.dynamicmap.DynamicStringMap;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicInteger;
import com.reedelk.runtime.api.service.ScriptEngineService;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.netty.http.server.HttpServerResponse;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static java.util.Optional.ofNullable;

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
     * server response object: it sets the values to be returned to the client.
     *
     * @param message        out flow Message
     * @param serverResponse http response to be sent back to the client
     * @param flowContext    the flow context object holding flow variables and other contextual info
     */
    public void map(Message message, HttpServerResponse serverResponse, FlowContext flowContext) {
        // 1. Status code
        DynamicInteger responseStatus = ofNullable(response).map(Response::getStatus).orElse(null);
        HttpResponseStatus status = EvaluateStatusCode.withDefault(OK)
                .withScriptEngine(scriptEngine)
                .withStatus(responseStatus)
                .withContext(flowContext)
                .withMessage(message)
                .evaluate();
        serverResponse.status(status);

        // 2. Content type
        DynamicByteArray responseBody = ofNullable(response).map(Response::getBody).orElse(null);
        ContentType.from(responseBody, message)
                .ifPresent(contentType -> serverResponse.addHeader(CONTENT_TYPE, contentType));

        // 3. Headers (which might override headers above)
        DynamicStringMap responseHeaders = ofNullable(response)
                .map(Response::getHeaders).orElse(null);
        AdditionalHeader.addAll(serverResponse, responseHeaders);
    }

    /**
     * Maps an exception to a not successful http response. It returns the byte stream
     * to be sent back to the client. This mapper DOES HAVE side effects on the HTTP
     * server response object: it sets the values to be returned to the client.
     *
     * @param exception      the exception we want to map to the HTTP server response
     * @param serverResponse http response to be sent back to the client
     * @param flowContext    the flow context object holding flow variables and other contextual info
     */
    public void map(Throwable exception, HttpServerResponse serverResponse, FlowContext flowContext) {
        // 1. Status code
        DynamicInteger errorResponseStatus = ofNullable(errorResponse).map(ErrorResponse::getStatus).orElse(null);
        HttpResponseStatus status = EvaluateStatusCode.withDefault(INTERNAL_SERVER_ERROR)
                .withScriptEngine(scriptEngine)
                .withStatus(errorResponseStatus)
                .withContext(flowContext)
                .withThrowable(exception)
                .evaluate();
        serverResponse.status(status);

        // 2. Response headers
        DynamicByteArray responseBody = ofNullable(errorResponse).map(ErrorResponse::getBody).orElse(null);
        if (responseBody != null && responseBody.isNotNull()) {
            // Content type is by default text if response is error (exception).
            // If the user wants to output JSON they must override with specific
            // additional headers the content type.
            serverResponse.addHeader(CONTENT_TYPE, MimeType.TEXT.toString());
        }

        // 3. User defined response headers (which might override headers above)
        DynamicStringMap responseHeaders = ofNullable(errorResponse)
                .map(ErrorResponse::getHeaders).orElse(null);
        AdditionalHeader.addAll(serverResponse, responseHeaders);
    }
}
