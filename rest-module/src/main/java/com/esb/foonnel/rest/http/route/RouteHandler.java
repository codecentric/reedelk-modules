package com.esb.foonnel.rest.http.route;


import com.esb.foonnel.api.message.Message;

public interface RouteHandler {

    Message handle(Message request) throws Exception;

}