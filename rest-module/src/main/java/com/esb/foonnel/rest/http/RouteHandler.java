package com.esb.foonnel.rest.http;


import com.esb.foonnel.api.message.Message;

public interface RouteHandler {

    Message handle(Message request) throws Exception;

}