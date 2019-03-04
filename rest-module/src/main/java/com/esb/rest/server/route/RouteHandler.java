package com.esb.rest.server.route;


import com.esb.api.message.Message;

public interface RouteHandler {

    Message handle(Message request) throws Exception;

}