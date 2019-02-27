package com.esb.foonnel.rest.http.server.route;


import com.esb.foonnel.api.message.Message;

public interface RouteHandler {

    Message handle(Message request) throws Exception;

}