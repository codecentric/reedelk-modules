package com.esb.rest.server.route;


import com.esb.api.component.OnResult;
import com.esb.api.message.Message;

public interface RouteHandler {

    void handle(Message request, OnResult callback) throws Exception;

}