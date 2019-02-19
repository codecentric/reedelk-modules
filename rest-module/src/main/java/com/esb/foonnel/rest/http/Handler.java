package com.esb.foonnel.rest.http;

import com.esb.foonnel.api.Message;

public interface Handler {

    Message handle(Request request, Response response) throws Exception;

}