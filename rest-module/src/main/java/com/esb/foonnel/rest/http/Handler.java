package com.esb.foonnel.rest.http;


import com.esb.foonnel.api.Message;

public interface Handler {

    Message handle(Message request) throws Exception;

}