package com.esb.rest.server.route;


import com.esb.api.component.ResultCallback;
import com.esb.api.message.Message;

public interface RouteHandler {

    void handle(Message request, ResultCallback callback) throws Exception;

}