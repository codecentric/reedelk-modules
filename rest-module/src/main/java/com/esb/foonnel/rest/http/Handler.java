package com.esb.foonnel.rest.http;

public interface Handler {

    Object handle(Request request, Response response) throws Exception;

}